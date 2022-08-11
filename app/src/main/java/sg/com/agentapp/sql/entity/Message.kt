package sg.com.agentapp.sql.entity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.BindingAdapter
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.work.WorkManager
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import sg.com.agentapp.R
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import sg.com.agentapp.global.GlideAPI.GlideApp
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.work_manager.WorkManagerHelper
import java.io.File
import java.io.Serializable
import java.util.*

@Entity(
        foreignKeys = [
    ForeignKey(
            entity = ChatList::class,
            parentColumns = ["chatJid"],
            childColumns = ["MsgJid"],
            onDelete = ForeignKey.CASCADE
    )]
)
class Message : Serializable {
    //===== IsSender rules: refer chatRoomAdapter

    //===== MsgOffline rules (ALL cases: +ve)
    // 0 = hvn't got ack from server
    // 1 = got ack from server (successfully sent to server)
    // *** incoming msges must ALWAYS equal 1 ***

    //===== MsgOffline rules (media cases: -ve)
    // 1 is end-goal for outgoing:
    // not compressed (-6) >
    // queued for compressing (-5) >
    // compressing (-4) >
    // compressed, not uploaded (-3) >
    // queued for uploading (-2) >
    // uploading (-1) >
    // uploaded/server acked (1)
    // uploaded, stanza will be sent by server, considered acked by server (1) = hide all

    // 1 is end-goal for incoming:
    // not downloaded (-3) >
    // queued for downloading (-2) >
    // downloading (-1) >
    // downloaded (1)

    @PrimaryKey(autoGenerate = true)
    var MsgRow: Int = 0
    var MsgJid: String? = null // room jid
    var IsSender: Int = 0
    var MsgDate: Long = 0L
    var MsgUniqueId: String? = null // stanza id
    var MsgData: String? = null // msg body (desc for media files)
    var MsgMediaPath: String? = null // media only - file path
    var MsgMediaInfo: String? = null // media only - imgThumb / audio length / doc name     / apptdata
    var MsgMediaResID: String? = null // media only - resourceID for downloading from server / apptid
    var MsgOffline: Int = 0 // offline status, media rules above
    var MsgFlagDate: Long = 0L // flag date (0 if not flagged)
    var MsgForward: Int = 0 // forwarded or not
    var MsgWorkID: String? = null // media only - workManager ID for compression/upload/download

    // reply msg
    var MsgReplyData: String? = null // msg reply body
    var MsgReplyJid: String? = null // msg reply jid (jid of old reply msg)
    var MsgReplyUniqueId: String? = null // msg reply/delete stanza id
    var MsgReplyMediaInfo: String? = null // msg reply imgThumb

    constructor()

    @Ignore
    constructor(msgJid: String, msgData: String) {
        MsgJid = msgJid
        MsgData = msgData
    }

    @Ignore
    constructor(msgJid: String, isSender: Int, msgDate: Long, msgUniqueId: String?, msgData: String?,
                msgOffline: Int, msgFlagDate: Long, msgMediaPath: String?, msgMediaInfo: String?,
                msgMediaResID: String?, msgWorkID: String?, msgForward: Int) {
        MsgJid = msgJid
        IsSender = isSender
        MsgDate = msgDate
        MsgUniqueId = msgUniqueId
        MsgData = msgData
        MsgMediaPath = msgMediaPath
        MsgMediaInfo = msgMediaInfo
        MsgMediaResID = msgMediaResID
        MsgOffline = msgOffline
        MsgFlagDate = msgFlagDate
        MsgForward = msgForward
        MsgWorkID = msgWorkID
    }

    // for reply msg
    @Ignore
    constructor(msgJid: String, isSender: Int, msgDate: Long, msgUniqueId: String?, msgData: String?,
                msgOffline: Int, msgReplyData: String?, msgReplyID: String?, msgReplyMediaInfo: String?,
                msgReplyJid: String) {
        MsgJid = msgJid
        IsSender = isSender
        MsgDate = msgDate
        MsgUniqueId = msgUniqueId
        MsgData = msgData
        MsgOffline = msgOffline
        MsgReplyJid = msgReplyJid
        MsgReplyData = msgReplyData
        MsgReplyUniqueId = msgReplyID
        MsgReplyMediaInfo = msgReplyMediaInfo
    }

    @Ignore
    constructor(MsgJid: String?, IsSender: Int, MsgDate: Long, MsgUniqueId: String?, MsgData: String?, apptId: String?, apptData: String?) {
        this.MsgJid = MsgJid
        this.IsSender = IsSender
        this.MsgDate = MsgDate
        this.MsgUniqueId = MsgUniqueId
        this.MsgData = MsgData
        this.MsgMediaInfo = apptData
        this.MsgMediaResID = apptId
    }


    //===== functions
    // get formatted date from long and return string
    fun getFormattedDate(l: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = l

        return DateFormat.format("h:mma", cal).toString()
    }

    // get date header item
    fun getDateHeader(): String {
        if (DateUtils.isToday(MsgDate)) {
            return "Today"
        } else {
            val cal = Calendar.getInstance()
            cal.timeInMillis = MsgDate

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }
    }

    // media upload/download btn function
    fun upDownBtnOnClick(view: View) {
        if (MsgOffline == -6 || MsgOffline == -3) { // not compressed/uploaded/downloaded, icon = start action
            // start upload/download action
            when (IsSender) {
                20, 22, 24 -> { // outgoing media - upload (already compressed)
                    WorkManagerHelper().enqueueUploadWork(MsgJid!!, MsgUniqueId!!, MsgMediaPath!!, IsSender)

                }

                21, 23, 25 -> { // incoming media - download
                    // check if got storage permissions first
                    if ((view.context as ChatRoom).checkPermissions(5)) {
                        // start download if got perm
                        WorkManagerHelper().enqueueDownloadWork(MsgJid!!, MsgUniqueId!!, MsgMediaResID!!, IsSender)
                    }
                }
            }
        } else { // compressing/uploading/downloading/in queue = stop action
            // cancel work based on workID
            if (MsgWorkID != null) {
                WorkManager.getInstance().cancelWorkById(UUID.fromString(MsgWorkID))
            }

            // update MsgOffline to -3 (compressed, not uploaded)
            DatabaseHelper.updateOfflineMsg(MsgJid!!, MsgUniqueId!!, -3)

        }
    }

    // media: doc
    // open file btn
    fun openDocBtn(view: View) {
        if ((view.context as ChatRoom).checkPermissions(5)) {
            val context = view.context

            val TAG = "JAY"
            // check if file exists first
            val file = File(MsgMediaPath)

            Log.d(TAG, "mediaPath = $MsgMediaPath")
            if (file.exists()) {
                // get file format from filePath
                val fileName = file.name
                val fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1)
                Log.d(TAG, "format = $fileFormat")

                // prep MimeType based on file format
                val myMime = MimeTypeMap.getSingleton()
                val newIntent = Intent(Intent.ACTION_VIEW)
                val mimeType = myMime.getMimeTypeFromExtension(fileFormat)

                // prep uri using file provider
                val fileURI: Uri = FileProvider.getUriForFile(
                        context,
                        context.getString(R.string.app_fp_name),
                        file
                )

                newIntent.setDataAndType(fileURI, mimeType)
                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                try {
                    context.startActivity(newIntent)

                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(view.context, R.string.no_app, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getDateStrFromLong(apptDateTime: Long?): String? {
        val a = ZonedDateTime.ofInstant(Instant.ofEpochMilli(apptDateTime!!), ZoneId.systemDefault().normalized())
        return a.format(DateTimeFormatter.ofPattern("dd MMM (EEE)"))
    }

    fun getTimeStrFromLong(apptDateTime: Long?): String? {
        val a = ZonedDateTime.ofInstant(Instant.ofEpochMilli(apptDateTime!!), ZoneId.systemDefault().normalized())
        return a.format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    fun getDateTime(apptDateTime: Long?): String? {
        val a = ZonedDateTime.ofInstant(Instant.ofEpochMilli(apptDateTime!!), ZoneId.systemDefault().normalized())
        return a.format(DateTimeFormatter.ofPattern("dd MMM (EEE), h:mm a"))
    }

}


//=====binding adapters
// glide imgPath to imgView (with file path AND file thumb)
@BindingAdapter("android:imgPath", "android:imgMediaInfo")
fun setChatImg(view: ImageView, imgPath: String?, mediaInfo: String?) {
    if (imgPath == null) {
        return
    }

    // use mediaInfo if imgFile no longer exists
    // convert str to base64 if needed
    if (imgPath.contains(".")) {
        // check if img file still exists
        if (!File(imgPath).exists()) { // not exist - use mediaInfo bytes
            GlideApp.with(view)
                    .load(Base64.decode(mediaInfo, Base64.DEFAULT))
                    .placeholder(R.drawable.default_img_400)
                    .dontTransform()
                    .centerCrop()
                    .into(view)

        } else { // exists - use imgFilePath
            GlideApp.with(view)
                    .load(imgPath)
                    .placeholder(R.drawable.default_img_400)
                    .dontTransform()
                    .centerCrop()
                    .thumbnail(0.5f)
                    .override(400, 400)
                    .encodeQuality(50)
                    .into(view)
        }

    } else {
        // need to use simple target for base64 to solve flashing problem
        GlideApp.with(view)
                .load(Base64.decode(imgPath, Base64.DEFAULT))
                .placeholder(R.drawable.default_img_400)
                .dontTransform()
                .centerCrop()
                .into(view)
    }
}

// glide imgPath to imgView (with media path column only)
@BindingAdapter("android:imgPathOnly")
fun setChatImg(view: ImageView, imgPath: String?) {
    if (imgPath == null) {
        return
    }

    if (imgPath.contains(".jpg")) {
        GlideApp.with(view)
                .load(imgPath)
                .placeholder(R.drawable.default_img_400)
                .dontTransform()
                .centerCrop()
                .thumbnail(0.5f)
                .override(100, 100)
                .encodeQuality(50)
                .into(view)

    } else {
        // need to use simple target for base64 to solve flashing problem
        GlideApp.with(view)
                .load(Base64.decode(imgPath, Base64.DEFAULT))
                .placeholder(R.drawable.default_img_400)
                .dontTransform()
                .centerCrop()
                .into(view)
    }
}

// glide binding adapter for profile imges
@BindingAdapter("android:proPic")
fun setProfilePic(view: ImageView, imgPath: String?) {
    if (imgPath == null) {
        return
    }

    // convert str to base64 if needed
    if (imgPath.contains(".jpg")) {
        // file, straight glide
        GlideApp.with(view)
                .load(imgPath)
                .placeholder(R.drawable.ic_profile_def_200px)
                .error(R.drawable.ic_profile_def_200px)
                .dontAnimate()
                .circleCrop()
                .into(view)
    } else {
        // need to use simple target for base64 to solve flashing problem
        GlideApp.with(view)
                .load(Base64.decode(imgPath, Base64.DEFAULT))
                .placeholder(R.drawable.ic_profile_def_200px)
                .error(R.drawable.ic_profile_def_200px)
                .dontAnimate()
                .circleCrop()
                .into(view)
    }
}

// set upload/download btn based on msgOffline
@BindingAdapter("android:upDownBtnMsgOffline", "android:upDownBtnIsSender")
fun setUpDownBtn(view: ImageView, msgOffline: Int, isSender: Int) {
    when (msgOffline) {
        // upload btn
        -6, // not compressed
        -3 // compressed, not uploaded/downloaded
        -> {
            when (isSender) {
                20, 22, 24 -> { // outgoing
                    view.setImageResource(R.drawable.ic_media_upload_gradient_100px)
                }

                21, 23, 25 -> { // incoming
                    view.setImageResource(R.drawable.ic_media_download_gradient_150px)

                }
            }
        }

        // cancel btn
        -5, // queued for compressing
        -4, // compressing
        -2, // queued for uploading/downloading
        -1 // uploading/downloading
        -> {
            view.setImageResource(R.drawable.ic_cancel_grey_100px)
        }

        1 -> { // action complete, change to play for audio
            when (isSender) {
                22, 23 -> {
                    view.setImageResource(R.drawable.ic_play_button_black)
                }
            }

        }

        else -> {
        }
    }
}