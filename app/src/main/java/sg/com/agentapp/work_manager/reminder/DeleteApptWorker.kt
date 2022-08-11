package sg.com.agentapp.work_manager.reminder

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import sg.com.agentapp.sql.DatabaseHelper


class DeleteApptWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val apptId = inputData.getString("apptId")

        DatabaseHelper.updateApptToExpired(apptId!!)

        return Result.success()
    }
}
