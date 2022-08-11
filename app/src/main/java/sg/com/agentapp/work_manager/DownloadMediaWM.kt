package sg.com.agentapp.work_manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import sg.com.agentapp.api.api_clients.RetroAPIClient.api
import sg.com.agentapp.api.api_clients.RetroS3Client.s3API
import sg.com.agentapp.global.DirectoryHelper
import sg.com.agentapp.global.MediaHelper
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.sql.DatabaseHelper
import java.io.*


class DownloadMediaWM(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    val TAG = "JAY"

    override fun doWork(): Result {
        Log.d(TAG, "DownloadMediaWM doWork")

        // get data for this work
        val jid = inputData.getString("jid")
        val msgID = inputData.getString("msgID")
        val resID = inputData.getString("resID")
        val isSender = inputData.getInt("isSender", -1)

        // update msgOffline (-1, downloading) to db
        DatabaseHelper.updateOfflineMsg(jid!!, msgID!!, -1)

        // get mediaURL from resID
        return getMediaURL(jid, msgID, isSender, resID!!)
    }

    // retro get media URL from resID
    private fun getMediaURL(jid: String, msgID: String, isSender: Int, resID: String): Result {
        // media type
        lateinit var mediaType: String
        when (isSender) {
            21 -> { // img
                mediaType = "image"
            }

            23 -> { // audio
                mediaType = "audio"
            }

            25 -> { // doc
                mediaType = "doc"
            }
        }

        // prepare form data (strings)
        val header = "Bearer ${Preferences.getInstance().accessToken}"
        val reqBody = JsonObject()
        reqBody.addProperty("resource_id", resID)
        reqBody.addProperty("media_type", mediaType)

        try {
            Log.d(TAG, "resID = $resID, mediaType = $mediaType")

            val response = api.getMediaURL(header, reqBody).execute()

            if (!response.isSuccessful) {
                DatabaseHelper.savePostLogsToDb("DownloadMediaWM getMediaURL resp unsuc", response.errorBody()?.string(), System.currentTimeMillis())

                return returnResult(jid, msgID, 0, null, isSender)
            }

            // download img with url
            var mediaURL = response.body()?.get("image_url")?.toString()
            var fileName = response.body()?.get("file_name")?.toString()

            Log.d(TAG, "DownloadMediaWM success 1 mediaURL = $mediaURL, fileName = $fileName")

            // download using retro GET
            mediaURL = mediaURL?.replace("\"", "")
            fileName = fileName?.replace("\"", "")
            return downloadMedia(jid, msgID, mediaURL!!, isSender, fileName)

        } catch (t: Throwable) {
            DatabaseHelper.savePostLogsToDb("DownloadMediaWM getMediaURL failure", t.toString(), System.currentTimeMillis())

            return returnResult(jid, msgID, 0, null, isSender)
        }
    }

    // download media via mediaURL
    private fun downloadMedia(jid: String, msgID: String, mediaURL: String, isSender: Int, fileName: String?): Result {
        Log.d(TAG, "downloadMedia mediaURL = $mediaURL")

        // edit mediaURL for retro
        val mediaURL2 = mediaURL.replace("https://agentapp-resources.s3.amazonaws.com/", "")
        Log.d(TAG, "mediaURL2 = $mediaURL2")

        try {
            val response = s3API.downloadMedia(mediaURL2).execute()

            if (!response.isSuccessful) {
                Log.d(TAG, "download failed = $response")

                DatabaseHelper.savePostLogsToDb("DownloadMediaWM downloadFromURL resp unsuc", response.errorBody()?.string(), System.currentTimeMillis())

                return returnResult(jid, msgID, 0, null, isSender)
            }

            val resp = response.body()
            Log.d(TAG, "resp =  $resp")

            // set filePath based on media type
            lateinit var fileFormatOrFullName: String

            when (isSender) {
                21 -> { // img
                    fileFormatOrFullName = "jpg"
                }

                23 -> { // audio
                    fileFormatOrFullName = "mp4"
                }

                25 -> { // doc
                    // media format for DOCS = full file name, since don't wanna change file name sent
                    fileFormatOrFullName = fileName!!
                }

                else -> {
                    fileFormatOrFullName = "jpg"

                }
            }

            Log.d(TAG, "DownloadMediaWM success 2 downloaded")

            // save file from resp
            return saveMediaToFile(jid, msgID, resp!!, fileFormatOrFullName, isSender)

        } catch (e: Exception) {
            DatabaseHelper.savePostLogsToDb("DownloadMediaWM downloadFromURL failure", e.toString(), System.currentTimeMillis())

            return returnResult(jid, msgID, 0, null, isSender)
        }
    }

    // save download media to file
    private fun saveMediaToFile(jid: String, msgID: String, body: ResponseBody, fileFormatOrFullName: String, isSender: Int): Result {
        try {
            // use directoryHelper to create file at correct path
            val file = DirectoryHelper().saveFile(applicationContext, isSender, fileFormatOrFullName)

            // return failure if file is null (no permissions)
            if (file == null) {
                // update msgOffline to -3 (not downloaded)
                DatabaseHelper.updateOfflineMsg(jid, msgID, -3)

                return Result.failure()
            }

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                // for progress
//                val fileSize = body.contentLength()
//                var fileSizeDownloaded: Long = 0

                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read = inputStream!!.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream.write(fileReader, 0, read)

//                    fileSizeDownloaded += read.toLong()

//                    Log.d(TAG, "file download: $fileSizeDownloaded of $fileSize")
                }

                outputStream.flush()

                return returnResult(jid, msgID, 1, file, isSender)

            } catch (e: IOException) {
                Log.e(TAG, "saveMediaToFile stream exception = $e")

                return returnResult(jid, msgID, 0, null, isSender)

            } finally {
                if (inputStream != null) {
                    inputStream.close()
                }

                if (outputStream != null) {
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "saveMediaToFile file exception = $e")

            return returnResult(jid, msgID, 0, null, isSender)
        }
    }

    fun returnResult(jid: String, msgID: String, result: Int, file: File?, isSender: Int?): Result {
        when (result) {
            0 -> { // retry
                // update msgOffline to -2 (queued for downloading)
                DatabaseHelper.updateOfflineMsg(jid, msgID, -2)

                return Result.failure()
            }

            1 -> { // success
                // do specific actions based on media type
                val filePath = file?.absolutePath.toString()
                var mediaInfo: String? = null

                when (isSender) {
                    21 -> { // imges: broadcast to gallery
                        applicationContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                    }

                    23 -> { // audio: get audio length
                        mediaInfo = MediaHelper().getMediaLength(applicationContext, file!!)
                    }

                    25 -> { // doc: get file name
                        mediaInfo = file?.name
                    }
                }

                // update msgOffline to 1 (downloaded) and filePath
                DatabaseHelper.updateMediaInMsg(jid, msgID, null, filePath, mediaInfo,
                        null, 1, "")

                return Result.success()
            }

            else -> { // retry
                // update msgOffline to -2 (queued for downloading)
                DatabaseHelper.updateOfflineMsg(jid, msgID, -2)

                return Result.retry()
            }
        }
    }

    override fun onStopped() {
        super.onStopped()

        Log.d(TAG, "DownloadMediaWM onStopped id = $id")

        // update MsgOffline to -3 (not downloaded)
//        DatabaseHelper.updateOfflineMsgFromWorkID(id.toString(), -3, true)
    }
}
