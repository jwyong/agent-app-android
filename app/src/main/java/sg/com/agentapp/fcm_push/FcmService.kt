package sg.com.agentapp.fcm_push

import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.BasicResponseModel
import sg.com.agentapp.api.api_models.SendFcmToken
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.sql.DatabaseHelper

class FcmService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        Log.i(TAG, "wtf fcm:${remoteMessage?.data.toString()} ")

        val i = FcmMessageModel.toModel(remoteMessage?.data)
        Log.i(TAG, "onMessageReceived: " + JSONObject(remoteMessage?.data))

        val jidhash = i.senderJid.hashCode()

        notificationAction(i)

//        var b = Bundle()
//        b.putString("jid", i.senderJid)
//        b.putString("name", i.title)
//        b.putString("phone", "")
//
//        Log.i(TAG, ":$b ")
//        val intent = Intent(this@FcmService, ChatRoom::class.java)
//        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
//        intent.putExtra("b", b)
//        val noti = NotificationCompat.Builder(this, "123")
//                .setContentIntent(PendingIntent
//                        .getActivity(this, jidhash, intent
//                                , PendingIntent.FLAG_ONE_SHOT, b))
//                .setSmallIcon(R.drawable.ic_logo)
//                .setContentTitle(i.title)
//                .setContentText(i.body)
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(jidhash, noti.build())
//        notificationManager.notify(123, Notification())
    }

    override fun onNewToken(s: String?) {
        super.onNewToken(s)

        RetroAPIClient.api.sendFcmToken(SendFcmToken(token = s),
                "Bearer ${Preferences.getInstance().accessToken}"
        ).enqueue(object : Callback<BasicResponseModel?> {
            override fun onFailure(call: Call<BasicResponseModel?>, t: Throwable) {
                Log.e(TAG, "fcmservice: ", t)
            }

            override fun onResponse(call: Call<BasicResponseModel?>, response: Response<BasicResponseModel?>) {
                if (!response.isSuccessful) {

                    Log.i(TAG, ":${response.body()?.message} ")
                    Log.i(TAG, ":${response.errorBody()?.string()} ")

                    return
                }

                Log.i("wtf", "fcm token sent: ")
            }
        })
    }

    companion object {
        private val TAG = "wtfFcmService"
    }


    fun notificationAction(pushBody: FcmMessageModel) {
        //get required info from push data payload
        val thread = pushBody.type?.split(":".toRegex())!!.dropLastWhile({ it.isEmpty() }).toTypedArray()
        val type = thread[0]

        if (type != null) { //safety unwrap
            val title = pushBody.title
            val body = pushBody.body
            val sender_jid = pushBody.senderJid

            when (type) {
                "chat" ->
                    if (!Preferences.getInstance().getValue("IndiPushNoti").equals("off")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            DatabaseHelper.saveNotiDataToDB(pushBody, "")
                            DatabaseHelper.push_bundle_for_NAbove(sender_jid!!)
                        } else {
                            DatabaseHelper.push_go_indi_chat(sender_jid!!, title!!, body!!, title)
                        }
                    }

//                "appointment" -> {
//                    val apptID: String?
//
//                    if (thread.size == 2) {
//                        apptID = thread[1]
//                    } else {
//                        apptID = null
//                    }
//                    if (!Preferences.getInstance().getValue(this, "IndiPushNoti").equals("off")) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            DatabaseHelper.saveNotiDataToDB(remoteMessage.data, 0, apptID)
//                            DatabaseHelper.pushMethodCaller(sender_jid, apptID)
//                        } else {
//                            DatabaseHelper.push_go_indi_sche(sender_jid, title, body, apptID)
//                        }
//                    }
//                }
            }
        }
    }
}