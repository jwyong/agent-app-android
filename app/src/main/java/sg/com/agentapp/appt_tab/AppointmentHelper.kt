package sg.com.agentapp.appt_tab

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.widget.Toast
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import sg.com.agentapp.AgentApp
import sg.com.agentapp.R
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.CreateApptReq
import sg.com.agentapp.api.api_models.CreateApptRes
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.global.UIHelper
import sg.com.agentapp.sql.DatabaseHelper
import java.util.*


class AppointmentHelper(private val activity: Activity) {

    fun acceptAppointment() {

    }

    fun rejectAppointment() {

    }

    fun updateAppointment(createApptReq: CreateApptReq, needPost: Boolean) {
        // ONLY post if needed (date/time updated)
        if (needPost) {
            val progressDialog = startProgDialog()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    var result = updateReq(createApptReq)
                    if (result.isSuccessful) {
                        updateApptDets(createApptReq, progressDialog, true)

                    } else {
                        val errJson = JSONObject(result.errorBody()!!.string())
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(AgentApp.instance?.applicationContext, "Unsuccessful. $errJson", Toast.LENGTH_SHORT).show()
                            stopProgDialog(progressDialog)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        stopProgDialog(progressDialog)
                        Toast.makeText(AgentApp.instance?.applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            updateApptDets(createApptReq, null, false)
        }

    }

    // update appt dets on post resp
    fun updateApptDets(createApptReq: CreateApptReq, progressDialog: Dialog?, needPost: Boolean) {
        // if need post means status must change to pending
        var status = -1
        if (needPost) {
            status = 2
        }

        // update new appt dets to db
        if (status == -1 ) {
            DatabaseHelper.updateApptNoStatus(
                    apptId = createApptReq.appointmentId!!,
                    date = createApptReq.date!!,
                    type = createApptReq.type!!,
                    roomType = createApptReq.roomType!!,
                    note = createApptReq.note,
                    reminder = createApptReq.reminder!!.toLong(),
                    lastUpdatorJid = createApptReq.lastUpdatorJid!!
            )

        } else {
            DatabaseHelper.updateAppt(
                    apptId = createApptReq.appointmentId!!,
                    date = createApptReq.date!!,
                    type = createApptReq.type!!,
                    roomType = createApptReq.roomType!!,
                    note = createApptReq.note,
                    reminder = createApptReq.reminder!!.toLong(),
                    lastUpdatorJid = createApptReq.lastUpdatorJid!!,
                    status = status,
                    isOut = true
            )
        }

        // update msg/reminder ONLY if date changed
        val apptOldDetail = DatabaseHelper.getApptDetail(createApptReq.appointmentId!!)
        if (apptOldDetail.ApptDateTime != createApptReq.date) {
            val gson = GsonBuilder().create()
            val json = gson.toJson(apptOldDetail)

            DatabaseHelper.updateApptMsg(createApptReq.appointmentId!!, apptOldDetail.ApptJid!!, true,
                    System.currentTimeMillis(), UUID.randomUUID().toString(), json, createApptReq.date)

            ApptReminderHelper().scheduleLocalNotification(createApptReq.appointmentId!!)
        }

        if (progressDialog != null) {
            stopProgDialog(progressDialog)
        }

        activity.finish()
    }

    private suspend fun updateReq(apptReq: CreateApptReq): Response<CreateApptRes> = RetroAPIClient.api.updateAppointment(
            "Bearer " + Preferences.getInstance().accessToken, apptReq
    )

    private suspend fun acceptReq(apptReq: CreateApptReq): Response<CreateApptRes> = RetroAPIClient.api.acceptAppointment(
            "Bearer " + Preferences.getInstance().accessToken, apptReq
    )

    private suspend fun rejectReq(apptReq: CreateApptReq): Response<CreateApptRes> = RetroAPIClient.api.rejectAppointment(
            "Bearer " + Preferences.getInstance().accessToken, apptReq
    )

    fun acceptRejectFunc(type: Int, apptId: String, needFinishActi: Boolean) {
        val progressDialog = startProgDialog()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var result: Response<CreateApptRes>? = null
                when (type) {
                    1 -> {
                        result = acceptReq(CreateApptReq(appointmentId = apptId,
                                messageId = UUID.randomUUID().toString(), resource = "ANDROID"))
                    }
                    2 -> {
                        result = rejectReq(CreateApptReq(appointmentId = apptId,
                                messageId = UUID.randomUUID().toString(), resource = "ANDROID"))
                    }
                }
//                val result = acceptReq(CreateApptReq(appointmentId = apptId,
//                        messageId = UUID.randomUUID().toString(), resource = "ANDROID"))

                if (result!!.isSuccessful) {
                    when (type) {
                        1 -> {
                            DatabaseHelper.upateApptStatus(apptId, 1, true, System.currentTimeMillis(), UUID.randomUUID().toString())
                        }
                        2 -> {
                            DatabaseHelper.upateApptStatus(apptId, 3, true, System.currentTimeMillis(), UUID.randomUUID().toString())
                        }
                    }
                    stopProgDialog(progressDialog)

                    if (needFinishActi) {
                        activity.finish()
                    }

                } else {
                    val errJson = JSONObject(result.errorBody()!!.string())
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(AgentApp.instance?.applicationContext, "Unsuccessful. $errJson", Toast.LENGTH_SHORT).show()
                        stopProgDialog(progressDialog)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    stopProgDialog(progressDialog)
                    Toast.makeText(AgentApp.instance?.applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startProgDialog(): Dialog = UIHelper().loadingDialog(activity, null)
    private fun stopProgDialog(progressDialog: Dialog) {
        CoroutineScope(Dispatchers.Main).launch {
            progressDialog.dismiss()
        }
    }

    fun getRoomTypeStringFromPos(str: String, c: Context): Int {
        val stringArray: Array<String> = c.resources.getStringArray(R.array.appt_roomtype)
        return stringArray.indexOf(str)
    }


    fun getRoomTypePosFromString(pos: Int, c: Context): String {
        val stringArray: Array<String> = c.resources.getStringArray(R.array.appt_roomtype)
        return stringArray[pos]
    }
}