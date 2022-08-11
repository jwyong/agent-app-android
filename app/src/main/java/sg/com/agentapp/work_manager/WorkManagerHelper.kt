package sg.com.agentapp.work_manager

import android.util.Log
import androidx.work.*
import sg.com.agentapp.AgentApp
import sg.com.agentapp.global.PermissionHelper
import sg.com.agentapp.sql.DatabaseHelper
import java.util.concurrent.TimeUnit

class WorkManagerHelper {
    val TAG = "JAY"

    // add upload work to workmanager queue
    fun enqueueUploadWork(jid: String, msgID: String, filePath: String, isSender: Int) {
        // prepare data for work
        val data = Data.Builder()
                .putString("jid", jid)
                .putString("msgID", msgID)
                .putString("filePath", filePath)
                .putInt("isSender", isSender)
                .build()

        // constraints to ONLY run when got internet
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        // build work request
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(UploadMediaWM::class.java)
                .setInputData(data)
                .addTag("UploadMedia")
                .setInitialDelay(1, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()

        Log.d(TAG, "upload work ID = ${oneTimeWorkRequest.id}")

        // update workID and msgOffline (queued for uploading, -2) to Msg DB
        DatabaseHelper.updateMediaInMsg(jid, msgID, null, null, null,
                null, -2, oneTimeWorkRequest.id.toString())

        // enqueue to workManager queue
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

    }

    // add upload work to workmanager queue
    fun enqueueDownloadWork(jid: String, msgID: String, resID: String, isSender: Int) {
        // don't enqueue if no storage permissions
        val permStrArr = arrayOf(
//                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!PermissionHelper().hasPermissions(AgentApp.instance!!.applicationContext, *permStrArr)) {
            return
        }

        // prepare data for work
        val data = Data.Builder()
                .putString("jid", jid)
                .putString("msgID", msgID)
                .putString("resID", resID)
                .putInt("isSender", isSender)
                .build()

        // constraints to ONLY run when got internet
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        // build work request
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(DownloadMediaWM::class.java)
                .setInputData(data)
                .addTag("DownloadMedia")
                .setInitialDelay(1, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()

        // update workID and msgOffline (queued for downloading, -2) to Msg DB
        DatabaseHelper.updateMediaInMsg(jid, msgID, null, null, null, null,
                -2, oneTimeWorkRequest.id.toString())

        // enqueue to workManager queue
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

    }
}