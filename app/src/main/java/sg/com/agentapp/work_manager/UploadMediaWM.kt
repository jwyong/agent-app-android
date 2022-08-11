package sg.com.agentapp.work_manager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.ImgProcessHelper
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.sql.DatabaseHelper
import java.io.File

class UploadMediaWM(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    val TAG = "JAY"
    override fun doWork(): Result {
        Log.d(TAG, "UploadMediaWM doWork")

        // get data for this work
        val jid = inputData.getString("jid")
        val msgID = inputData.getString("msgID")
        val filePath = inputData.getString("filePath")
        val isSender = inputData.getInt("isSender", -1)

        // update msgOffline to -1 (uploading)
        DatabaseHelper.updateOfflineMsg(jid!!, msgID!!, -1)

        // prep file name and format
        val fileName = filePath?.substring(filePath.lastIndexOf("/") + 1)
        val fileFormat = filePath?.substring(filePath.lastIndexOf(".") + 1)

        Log.d(TAG, "UploadMediaWM filePath = $filePath, fileName = $fileName, fileFormat = $fileFormat")

        // upload via retro
        val api = RetroAPIClient.api

        // prepare form data (based on type)
        lateinit var mediaType: String
        lateinit var recvSubj: String
        lateinit var mediaParseStr: String
        lateinit var msgBody: String

        when (isSender) {
            20 -> { // img
                mediaType = "image"
                mediaParseStr = "image/*"
                recvSubj = GlobalVars.MEDIA_IMG_RECEIVED
                msgBody = GlobalVars.MEDIA_IMG_SENT
            }

            22 -> { // audio
                mediaType = "audio"
                mediaParseStr = "audio/*"
                recvSubj = GlobalVars.MEDIA_AUDIO_RECEIVED
                msgBody = GlobalVars.MEDIA_AUDIO_SENT
            }

            24 -> { // doc
                mediaType = "doc"
                mediaParseStr = "text/*"
                recvSubj = GlobalVars.MEDIA_DOC_RECEIVED
                msgBody = GlobalVars.MEDIA_DOC_SENT
            }

            else -> {
                mediaType = "image"
                mediaParseStr = "image/*"
                recvSubj = GlobalVars.MEDIA_IMG_RECEIVED
                msgBody = GlobalVars.MEDIA_IMG_SENT
            }
        }

        // prepare form data (strings)
        val header = "Bearer ${Preferences.getInstance().accessToken}"
        val mediaTypePart = MultipartBody.Part.createFormData("media_type", mediaType)
        val msgIDPart = MultipartBody.Part.createFormData("message_id", msgID)
        val senderJidPart = MultipartBody.Part.createFormData("sender_jid", Preferences.getInstance().userXMPPJid)
        val jidPart = MultipartBody.Part.createFormData("receiver_jid", jid)
        val resPart = MultipartBody.Part.createFormData("resources", "ANDROID")
        val subjPart = MultipartBody.Part.createFormData("subject", recvSubj)
        val bodyPart = MultipartBody.Part.createFormData("body", Preferences.getInstance().agentName)
        val fileNamePart = MultipartBody.Part.createFormData("file_name", fileName!!)

        // prepare form data (img only - base64 string)
        var base64Str: String? = null
        if (isSender == 20) {
            base64Str = ImgProcessHelper().createBase64Thumb(filePath, 25)

            // if base64 null means no file, stop upload
            if (base64Str == null) {
                DatabaseHelper.updateOfflineMsg(jid, msgID, -3)

                DatabaseHelper.savePostLogsToDb("UploadMediaWM", "base64Str null", System.currentTimeMillis())

                return Result.failure()
            }
        }
        val imgThumbPart = MultipartBody.Part.createFormData("image_thumbnail", base64Str ?: "")

        // prepare form data (media file)
        val mediaFile = File(filePath)
        val fileReqBody = RequestBody.create(MediaType.parse(mediaParseStr), mediaFile)
        val file = MultipartBody.Part.createFormData("file", fileFormat, fileReqBody)

        try {
            val response = api.uploadMedia(header, mediaTypePart, msgIDPart, senderJidPart, jidPart, resPart, subjPart,
                    bodyPart, imgThumbPart, fileNamePart, file).execute()

            if (!response.isSuccessful) {
                // update msgOffline to -2 (queued for uploading)
                DatabaseHelper.updateOfflineMsg(jid, msgID, -2)

                DatabaseHelper.savePostLogsToDb("uploadImgRetro resp unsuc", response.errorBody()?.string(), System.currentTimeMillis())

                return Result.failure()

            }

            var resID = response.body()?.get("resource_id").toString()
            resID = resID.replace("\"", "")

            // update MsgOffline to 1 (uploaded and server acked) + resID to db
            DatabaseHelper.updateMediaInMsg(jid, msgID, msgBody, null, null,
                    resID, 1, "")

            return Result.success()

        } catch (e: Exception) {
            // update msgOffline to -3 (not uploaded)
            DatabaseHelper.updateOfflineMsg(jid, msgID, -3)

            DatabaseHelper.savePostLogsToDb("uploadImgRetro failure", e.toString(), System.currentTimeMillis())

            return Result.failure()
        }
    }
}
