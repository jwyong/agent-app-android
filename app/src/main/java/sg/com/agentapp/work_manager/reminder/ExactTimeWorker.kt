package sg.com.agentapp.work_manager.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import sg.com.agentapp.AgentApp
import sg.com.agentapp.R
import sg.com.agentapp.home.HomeActivity
import sg.com.agentapp.sql.DatabaseHelper
import java.util.concurrent.TimeUnit


class ExactTimeWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {
        val apptId = inputData.getString("apptId")
        val apptDetail = DatabaseHelper.getApptWithAgentDetail(apptId!!)
        val context = AgentApp.instance!!.applicationContext

        val intentHome = Intent(context, HomeActivity::class.java)
        intentHome.putExtra("remoteSche", "1")

        val pendingIntent = PendingIntent.getActivity(context, 0, intentHome,
                PendingIntent.FLAG_ONE_SHOT)

        val lights = TimeUnit.SECONDS.toMillis(1).toInt()


//        val wazeIntent = Intent("com.soapp.MsgReply")
//        wazeIntent.putExtra("type", "waze")
//        wazeIntent.putExtra("lat", apptDetail.ApptLatitude)
//        wazeIntent.putExtra("lon", apptDetail.ApptLongitude)
//        wazeIntent.putExtra("apptID", apptId)
//
//        val wazePendingIntent = PendingIntent.getBroadcast(context,
//                ("wazeReminder" + apptId).hashCode(),
//                wazeIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val gmapIntent = Intent("com.soapp.MsgReply")
//        gmapIntent.putExtra("type", "gmaps")
//        gmapIntent.putExtra("lat", apptDetail.ApptLatitude)
//        gmapIntent.putExtra("lon", apptDetail.ApptLongitude)
//        gmapIntent.putExtra("apptID", apptId)
//
//        val gmapsPendingIntent = PendingIntent.getBroadcast(context,
//                "gmapReminder$apptId".hashCode(),
//                gmapIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val replyAction = NotificationCompat.Action.Builder(R.drawable.ic_green_dot_42px,
//                "Waze", wazePendingIntent)
//        val naviAction = NotificationCompat.Action.Builder(R.drawable.ic_green_dot_42px,
//                "Google Maps", gmapsPendingIntent)

        val notificationBuilder = NotificationCompat.Builder(context, "Reminder")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round))
                .setColor(context.resources.getColor(R.color.colorPrimary))
                .setContentTitle(String.format("%s with %s", apptDetail.appt.ApptSubject, apptDetail.cr[0].CRName))
                .setContentText(apptDetail.appt.ApptSubject + " is happening now")
                .setAutoCancel(true)
                .setGroup("Reminder")
                .setContentIntent(pendingIntent)
                .setLights(Color.BLUE, lights, lights)

        val id = String.format("reminder-%s", apptId).hashCode()
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify("ApptReminder", id, notificationBuilder.build())

        return Result.success()
    }
}
