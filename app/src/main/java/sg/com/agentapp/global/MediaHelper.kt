package sg.com.agentapp.global

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.work_manager.WorkManagerHelper
import java.io.File
import java.util.*

class MediaHelper {
    val TAG = "JAY"

    // get time in str of mm:ss based on millis
    fun timeIntToStr(timeInt: Int, isMilli: Boolean): String {
        val timeInSec: Int

        if (isMilli) {
            timeInSec = timeInt / 1000
        } else {
            timeInSec = timeInt
        }

        // get min and sec in int (within 60)
        val timerMinInt = Math.abs(Math.floor((timeInSec / 60).toDouble())).toInt()
        val timerSecInt = timeInSec - timerMinInt * 60

        // format to double digit str
        val timerMinStr = timeDigitIntToStr(timerMinInt)
        val timerSecStr = timeDigitIntToStr(timerSecInt)

        // return "stopped" if got null (more than 1 hour)
        if (timerMinStr == null) {
            return "Stopped"
        } else {
            return "$timerMinStr:$timerSecStr"
        }
    }

    // time digit int to str (for each digit either minute or second
    fun timeDigitIntToStr(intVal: Int): String? {
        if (intVal <= 9) { // single digit
            return "0$intVal"
        } else if (intVal < 60) { // double digit
            return intVal.toString()
        } else { // +1
            return null
        }
    }

    // get length of audio file in "mm:ss"
    fun getMediaLength(context: Context, file: File): String? {
        val retriever = MediaMetadataRetriever()

        retriever.setDataSource(context, Uri.fromFile(file))
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
        retriever.release()

        // convert time from int (millis) to str ("mm:ss")
        return timeIntToStr(time, true)
    }

    // function for compressing and sending img (outgoing)
    fun compressAndSendImg(context: Context, oriFile: File, jid: String, name: String, phone: String,
                           profilePicUrl: String) {
        // save msg to db based on oriFile path first (uncompressed, offline = -6)
        // msgID = jid + UUID for ALL media files
        val msgID = jid + UUID.randomUUID().toString()

        // check and insert to CR
        DatabaseHelper.checkAndAddtoCR(jid, name, phone, profilePicUrl)

        // insert to msg
        val imgProcessHelper = ImgProcessHelper()
        DatabaseHelper.outMedia(jid, msgID, System.currentTimeMillis(), GlobalVars.MEDIA_IMG_SENT,
                oriFile.absolutePath.toString(), null, null, 20, -6, 0)

        // compress image - outgoing compress img = 1
        var compressedFile = imgProcessHelper.compressImg(context, oriFile, 1, 720, 75)

        // null means no need compress, so straight use oriFile
        if (compressedFile == null) {
            compressedFile = oriFile
        }

        // update offlinemsg to compressed, not uploaded (-3) + img thumb
        val filePath = compressedFile.absolutePath.toString()
        val imgThumb = imgProcessHelper.createBase64Thumb(filePath, 25)
        DatabaseHelper.updateMediaInMsg(jid, msgID, null, filePath, imgThumb,
                null, -3, null)

        // enqueue upload work (mediaType = img (0))
        WorkManagerHelper().enqueueUploadWork(jid, msgID, filePath, 20)
    }

    // function for sending docs (no compress)
    fun sendMediaFile(context: Context, file: File, jid: String, name: String, phone: String,
                      profilePicUrl: String, isSender: Int) {
        // check and insert to CR
        DatabaseHelper.checkAndAddtoCR(jid, name, phone, profilePicUrl)

        // prep vars based on isSender
        val filePath = file.absolutePath.toString()
        val msgBody: String
        var mediaInfo: String? = null
        when (isSender) {
            22 -> { // audio
                msgBody = GlobalVars.MEDIA_AUDIO_SENT

                // audio length
                mediaInfo = getMediaLength(context, file)
            }

            24 -> { // doc
                msgBody = GlobalVars.MEDIA_DOC_SENT

                // file name
                mediaInfo = file.name
            }

            else -> {
                msgBody = GlobalVars.MEDIA_SENT
            }
        }

        // insert to msg
        val msgID = UUID.randomUUID().toString()
        DatabaseHelper.outMedia(jid, msgID, System.currentTimeMillis(), msgBody, filePath, mediaInfo,
                null, isSender, -3, 0)

        // enqueue upload work (mediaType = img (0))
        WorkManagerHelper().enqueueUploadWork(jid, msgID, filePath, isSender)
    }
}