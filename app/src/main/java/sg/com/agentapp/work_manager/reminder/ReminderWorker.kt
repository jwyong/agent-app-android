package sg.com.agentapp.work_manager.reminder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import sg.com.agentapp.AgentApp
import sg.com.agentapp.R
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.home.HomeActivity
import sg.com.agentapp.sql.DatabaseHelper
import java.util.concurrent.TimeUnit


class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {

        val preferences = Preferences.getInstance()
        val context = AgentApp.instance!!.applicationContext
        val data = inputData
        var time = data.getLong("time", -1)

        val apptId = data.getString("apptId")
        val apptDetail = DatabaseHelper.getApptWithAgentDetail(apptId!!)

//        val prefTime = preferences.getIntValue(context, GlobalVariables.STRPREF_NOTIFICATION_TIME)


        if (preferences.getValue("ApptReminder") != "off") { //if reminder is on
            val content0: String

//            if (time == -1L) {
//                time = (if (prefTime < 0) GlobalVariables.defaultReminderAlert else prefTime).toLong()
//            }

            if (time == 0L) {
                content0 = context.getString(R.string.appt_now)
            } else if (time < 60) { //less than an hour, use "minutes"
                content0 = context.getString(R.string.appt_in) + " " + time + " " +
                        "" + context.getString(R.string.appt_minutes)
            } else if (time == 60L) { //1 hour
                content0 = context.getString(R.string.appt_in) + " 1 " + context.getString(R
                        .string.appt_hour)
            } else if (time < 2880) { //less than 2 days, use "hours" e.g. 24 hours
                content0 = context.getString(R.string.appt_in) + " " + time / 60 + " " +
                        context.getString(R.string.appt_hours)
            } else if (time < 10080) { //less than a week, use "days"
                content0 = context.getString(R.string.appt_in) + " " + time / 1440 +
                        " " + context.getString(R.string.appt_days)
            } else { //a week
                content0 = context.getString(R.string.appt_in) + " 1 " + context.getString(R.string.appt_week)
            }
//            val savedSoundUri = Uri.parse("android.resource://com.soapp/" + R.raw.soapp_appt)
//            val vibrate = longArrayOf(300, 300)

            val intentHome = Intent(context, HomeActivity::class.java)
            intentHome.putExtra("remoteSche", "1")

            val pendingIntent = PendingIntent.getActivity(context, 0,
                    intentHome,
                    PendingIntent.FLAG_ONE_SHOT)

            val lights = TimeUnit.SECONDS.toMillis(1).toInt()

            //            RemoteInput remoteInputStatus = new RemoteInput.Builder("ReplyReminder")
            //                    .setLabel("Waze")
            //                    .setAllowFreeFormInput(false)
            //                    .setChoices(new String[]{"Going", "Not Going", "Undecided"})
            //
            //                    .build();
            //
            //            RemoteInput remoteInputNavi = new RemoteInput.Builder("ReplyReminder")
            //                    .setAllowFreeFormInput(false)
            //                    .setLabel("Navigate")
            //                    .setChoices(new CharSequence[]{"Waze", "Gmaps"})
            //                    .build();

            val wazeIntent = Intent("com.soapp.MsgReply")
            wazeIntent.putExtra("type", "waze")
            wazeIntent.putExtra("lat", apptDetail.appt.ApptLatitude)
            wazeIntent.putExtra("lon", apptDetail.appt.ApptLongitude)
            wazeIntent.putExtra("apptID", apptId)

            val wazePendingIntent = PendingIntent.getBroadcast(context,
                    ("wazeReminder" + apptId).hashCode(),
                    wazeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            val gmapIntent = Intent("com.soapp.MsgReply")
            gmapIntent.putExtra("type", "gmaps")
            gmapIntent.putExtra("lat", apptDetail.appt.ApptLatitude)
            gmapIntent.putExtra("lon", apptDetail.appt.ApptLongitude)
            gmapIntent.putExtra("apptID", apptId)

            val gmapsPendingIntent = PendingIntent.getBroadcast(context,
                    "gmapReminder$apptId".hashCode(),
                    gmapIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            val replyAction = Notification.Action.Builder(R.drawable.mapbox_compass_icon,
                    "Waze", wazePendingIntent)
            val naviAction = Notification.Action.Builder(R.drawable.mapbox_compass_icon,
                    "Google Maps", gmapsPendingIntent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                replyAction.setAllowGeneratedReplies(true)
                naviAction.setAllowGeneratedReplies(true)
            }

            var notificationBuilder: Notification.Builder? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                notificationBuilder = Notification.Builder(context, "Reminder")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round))
                        .setColor(context.resources.getColor(R.color.colorPrimary))
                        .setContentTitle(String.format("%s with %s", apptDetail.appt.ApptSubject,
                                apptDetail.cr[0].CRName))
                        .setContentText(content0)
                        .setAutoCancel(true)
                        .setGroup("Reminder")
                        .setContentIntent(pendingIntent)
                        .setActions(replyAction.build(), naviAction.build())
                        .setStyle(Notification.InboxStyle())
                        .setLights(Color.BLUE, lights, lights)
            } else {
                notificationBuilder = Notification.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round))
                        .setColor(context.resources.getColor(R.color.colorPrimary))
                        .setContentTitle(String.format("%s with %s", apptDetail.appt.ApptSubject,
                                apptDetail.cr[0].CRName))
                        .setContentText(content0)
                        .setAutoCancel(true)
                        .setGroup("Reminder")
                        .setContentIntent(pendingIntent)
                        .addAction(replyAction.build())
                        .addAction(naviAction.build())
                        .setLights(Color.BLUE, lights, lights)
            }

            val id = String.format("reminder-%s", apptId).hashCode()
            val nm = context
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify("ApptReminder", id, notificationBuilder!!.build())
            nm.notify("ApptReminder", id, notificationBuilder.build())

        }

        return Result.success()
    }
}
