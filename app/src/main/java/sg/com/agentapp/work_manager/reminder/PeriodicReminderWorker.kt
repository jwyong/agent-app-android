package sg.com.agentapp.work_manager.reminder

import android.content.ContentValues
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import sg.com.agentapp.appt_tab.ApptReminderHelper
import sg.com.agentapp.sql.DatabaseHelper
import java.util.concurrent.TimeUnit


class PeriodicReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {


        val endTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(30)
        val list = DatabaseHelper.getRecentAppttoRemind(endTime)
        val cv = ContentValues()

        WorkManager.getInstance().cancelAllWorkByTag("ReminderAppt") //XX minutes before appt
        WorkManager.getInstance().cancelAllWorkByTag("ExactReminderAppt") //appt happening now
        WorkManager.getInstance().cancelAllWorkByTag("DeleteReminderAppt") //shift appt to History XX hours after appt


        var count = 0
        for (appt in list) {
            cv.clear()
            if (appt.ApptDateTime!! < System.currentTimeMillis()) {

                DatabaseHelper.updateApptToExpired(appt.ApptId!!)
            } else {
                count = +ApptReminderHelper().scheduleLocalNotification(appt.ApptId!!)
            }

        }

        //show alert dialog if need
//        if (count > 0 && inputData.getBoolean("needAlertDialog", false)) {
//            val context = AgentApp.instance!!.applicationContext
//
//            //get pref and use global func to convert to readable time string
//            //            String readerTime = ;
//
//            val msg = String.format(Locale.ENGLISH, "There %s %d %s upcoming", if (count == 1) "is" else "are",
//                    count, if (count == 1) "appointment" else "appointments")
//
//            UIHelper().dialog2Btns(context, context.getString(R.string.appt_next_reminder_title),
//                    msg, R.string.ok_label, R.string.cancel, R.color.white, R.color.primaryDark3, null, null, true)
//        }

        return Result.success()
    }

}
