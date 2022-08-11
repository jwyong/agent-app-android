package sg.com.agentapp.appt_tab

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.work.*
import sg.com.agentapp.R
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.MiscHelper
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.DatabaseHelper.getApptWorkIDExits
import sg.com.agentapp.sql.entity.ApptWorkUUID
import sg.com.agentapp.work_manager.reminder.DeleteApptWorker
import sg.com.agentapp.work_manager.reminder.ExactTimeWorker
import sg.com.agentapp.work_manager.reminder.ReminderWorker
import java.util.*
import java.util.concurrent.TimeUnit

class ApptReminderHelper {

    val AWID_TABLE_APPTWORKUUID = "ApptWorkUUID"
    val AWID_COLUMN_APPTWORKROW = "ApptWorkRow"
    val AWID_COLUMN_APPTID = "ApptID"
    val AWID_COLUMN_REMINDERUUID = "reminderUUID"
    val AWID_COLUMN_EXACTUUID = "exactUUID"
    val AWID_COLUMN_DELETEUUID = "deleteUUID"

    fun getReminderStringPosFromMilli(milli: Long, c: Context): Int {
        val stringArray: Array<String> = c.resources.getStringArray(R.array.reminder_time)
        val intArray: IntArray = c.resources.getIntArray(R.array.appt_reminder_milli)

        val pos = intArray.indexOf(milli.toInt())
//        return stringArray[pos]
        return pos
    }


    fun getReminderMillFromString(str: String, c: Context): Int {
        val stringArray: Array<String> = c.resources.getStringArray(R.array.reminder_time)
        val intArray: IntArray = c.resources.getIntArray(R.array.appt_reminder_milli)

        val pos = stringArray.indexOf(str)
        return intArray[pos]
//        return pos
    }

    fun getReminderMillFromStringPos(strPos: Int, c: Context): Long {
        val stringArray: Array<String> = c.resources.getStringArray(R.array.reminder_time)
        val intArray: IntArray = c.resources.getIntArray(R.array.appt_reminder_milli)

//        val pos = stringArray.indexOf(str)
        return intArray[strPos].toLong()
//        return pos
    }

    fun scheduleLocalNotification(apptID: String): Int {
        val list = DatabaseHelper.getApptDetail(apptID)
        if (list.ApptDateTime!! < System.currentTimeMillis() + TimeUnit.HOURS.toMillis(30)) {
            val apptWorkUUID = DatabaseHelper.getApptWorkUUID(apptID)

            Log.d(GlobalVars.I_LOG, "ApptReminderHelper scheduleLocalNotification:  ${list.getFormattedTime(list.ApptDateTime!!)}")

            Log.d(GlobalVars.I_LOG, "ApptReminderHelper scheduleLocalNotification:  $list")
            Log.d(GlobalVars.I_LOG, "ApptReminderHelper scheduleLocalNotification:  $apptWorkUUID")

            //if not going NO NEED reminder
            var count = 0

            // only set reminder for pending AND confirmed appts
            if (list.ApptStatus != 3) {
                val reminderTime = list.ApptReminderTime
                count = setReminderUsingWorker(apptID, list.ApptDateTime, reminderTime!!, if (apptWorkUUID != null) apptWorkUUID.reminderUUID else null, list.ApptJid!!)

                exactTimeApptReminder(apptID, list.ApptDateTime, if (apptWorkUUID?.apptWorkRow != null) apptWorkUUID.exactUUID else null, list.ApptJid!!)
            }

            // set "expired" after xx hours
            setClearAfterXXhrWorker(apptID, list.ApptDateTime, if (apptWorkUUID?.apptWorkRow != null) apptWorkUUID.deleteUUID else null)

            return count
        }
        return 0
    }

    fun setReminderUsingWorker(apptID: String, apptime: Long?, customReminderTime: Long, uuid: String?, jid: String): Int {
        var customReminderTime = customReminderTime
        if (uuid != null && !uuid.isEmpty()) {
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuid))
        }
        //get from db, no value take from prefrences
//        customReminderTime = if (customReminderTime < 0) TimeUnit.MINUTES.toMillis() else customReminderTime

        //if preferences -1 (null) take from default : 60min
//        customReminderTime = if (customReminderTime < 0) TimeUnit.MINUTES.toMillis(60) else customReminderTime

        // milliseconds from now until reminder
        val remindertime = apptime!! - customReminderTime - System.currentTimeMillis()

        Log.d(GlobalVars.I_LOG, "ApptReminderHelper setReminderUsingWorker:  ${MiscHelper().getHumanTimeFormatFromMilliseconds(remindertime)}")

        //if reminder time is already in the past, just alert user count
        var pastApptReminderCount = 0
        if (remindertime < 0) {
            pastApptReminderCount++

        } else {
            val data = Data.Builder()
                    .putString("apptId", apptID)
                    .putLong("time", TimeUnit.MILLISECONDS.toMinutes(customReminderTime))
                    .putString("jid", jid)
                    .build()

            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
                    .setInputData(data)
                    .addTag("ReminderAppt")
                    .setInitialDelay(remindertime, TimeUnit.MILLISECONDS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                    .build()

            WorkManager.getInstance().enqueue(oneTimeWorkRequest)

            val cv = ContentValues()
            cv.put(AWID_COLUMN_REMINDERUUID, oneTimeWorkRequest.id.toString())

            if (getApptWorkIDExits(apptID))
                DatabaseHelper.updateDB1Col(AWID_TABLE_APPTWORKUUID, cv, AWID_COLUMN_APPTID, apptID)
            else
                DatabaseHelper.insertApptWorkId(ApptWorkUUID(apptID, cv.getAsString(AWID_COLUMN_REMINDERUUID), "", ""))
        }

        return pastApptReminderCount
    }

    fun exactTimeApptReminder(apptId: String, apptime: Long?, uuid: String?, jid: String) {
        if (uuid != null && !uuid.isEmpty()) {
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuid))
        }

        val data = Data.Builder()
                .putString("apptId", apptId)
                .putString("jid", jid)
                .build()

        val delaytime = apptime!! - System.currentTimeMillis()

        Log.d(GlobalVars.I_LOG, "ApptReminderHelper exactTimeApptReminder: ${MiscHelper().getHumanTimeFormatFromMilliseconds(delaytime)} ")

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ExactTimeWorker::class.java)
                .setInputData(data)
                .addTag("ExactReminderAppt")
                .setInitialDelay(delaytime, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build()

        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

        val cv = ContentValues()
        cv.put(AWID_COLUMN_EXACTUUID, oneTimeWorkRequest.id.toString())

        if (getApptWorkIDExits(apptId))
            DatabaseHelper.updateDB1Col(AWID_TABLE_APPTWORKUUID, cv, AWID_COLUMN_APPTID, apptId)
        else
            DatabaseHelper.insertApptWorkId(ApptWorkUUID(apptId, "", cv.getAsString(AWID_COLUMN_EXACTUUID), ""))

    }

    fun setClearAfterXXhrWorker(apptId: String, apptime: Long?, uuid: String?) {

        if (uuid != null && !uuid.isEmpty()) {
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuid))
        }

        val data = Data.Builder()
                .putString("apptId", apptId)
                .build()

        // time for deletion
        val delaytime = apptime!! + TimeUnit.HOURS.toMillis(1) - System.currentTimeMillis()

        Log.d(GlobalVars.I_LOG, "ApptReminderHelper setClearAfterXXhrWorker:  ${MiscHelper().getHumanTimeFormatFromMilliseconds(delaytime)}")

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(DeleteApptWorker::class.java)
                .setInputData(data)
                .addTag("DeleteReminderAppt")
                .setInitialDelay(delaytime, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build()

        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

        val cv = ContentValues()
        cv.put(AWID_COLUMN_DELETEUUID, oneTimeWorkRequest.id.toString())
        DatabaseHelper.updateDB1Col(AWID_TABLE_APPTWORKUUID, cv, AWID_COLUMN_APPTID, apptId)

        if (getApptWorkIDExits(apptId))
            DatabaseHelper.updateDB1Col(AWID_TABLE_APPTWORKUUID, cv, AWID_COLUMN_APPTID, apptId)
        else
            DatabaseHelper.insertApptWorkId(ApptWorkUUID(apptId, "", "", cv.getAsString(AWID_COLUMN_DELETEUUID)))
    }
}