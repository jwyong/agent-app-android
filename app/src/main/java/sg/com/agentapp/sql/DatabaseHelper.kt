package sg.com.agentapp.sql

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.Maybe
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sg.com.agentapp.AgentApp
import sg.com.agentapp.R
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.CreateApptReq
import sg.com.agentapp.fcm_push.FcmMessageModel
import sg.com.agentapp.global.DownloadProfileFromUrl
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.home.HomeActivity
import sg.com.agentapp.sql.entity.*
import sg.com.agentapp.sql.joiner.ApptWithCR
import sg.com.agentapp.work_manager.WorkManagerHelper
import sg.com.agentapp.xmpp.outgoing.SingleChatStanza
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object DatabaseHelper {
    val TAG = "JAY"
    private var db: AADatabase?
    private var sdb: SupportSQLiteDatabase

    private var preferences: Preferences
    // ===== CR LABELS =====
    const val CR_TABLE_NAME = "ContactRoster"
    const val CR_COL_ROW = "CRRow"
    const val CR_COL_JID = "CRJid"
    const val CR_COL_NAME = "CRName"
    const val CR_COL_PHONE = "CRPhoneNumber"
    const val CR_COL_RES_ID = "CRResourceID"
    const val CR_COL_PROILE_PIC = "CRProfilePic"
    const val CR_COL_BLOCKED = "CRBlocked"


    // ===== CHATLIST LABELS =====
    const val CL_TABLE_NAME = "ChatList"
    const val CL_COL_ROW = "chatRow"
    const val CL_COL_JID = "chatJid"
    const val CL_COL_DATE = "chatDate"
    const val CL_COL_NOTI_BADGE = "chatNotiBadge"
    const val CL_COL_LAST_MSG = "chatLastMsg"
    const val CL_COL_TYPING = "chatTyping"


    // ===== MESSAGE LABELS =====
    const val MSG_TABLE_NAME = "Message"
    const val MSG_COLUMN_JID = "MsgJid"
    const val MSG_COLUMN_ISSENDER = "IsSender"
    const val MSG_COLUMN_ID = "MsgUniqueId"
    const val MSG_COLUMN_DATA = "MsgData"
    const val MSG_COLUMN_PATH = "MsgMediaPath"
    const val MSG_COLUMN_MEDIA_INFO = "MsgMediaInfo"
    const val MSG_COLUMN_RES_ID = "MsgMediaResID"
    const val MSG_COLUMN_OFFLINE = "MsgOffline"
    const val MSG_COLUMN_WORK_ID = "MsgWorkID"
    const val MSG_COLUMN_FLAGGED = "MsgFlagDate"
    const val MSG_COLUMN_REPLY_DATA = "MsgReplyData"
    const val MSG_COLUMN_REPLY_ID = "MsgReplyUniqueId"

    // ===== APPT LABELS =====


    // ===== NOTI VAR =====
    private lateinit var prevChatJID: String

    var titleChat1: String = ""
    var titleChat2: String = ""
    var titleChat3: String = ""
    var titleChat4: String = ""
    var titleChat5: String = ""

    var contentChat1: String = ""
    var contentChat2: String = ""
    var contentChat3: String = ""
    var contentChat4: String = ""
    var contentChat5: String = ""
    // ===== NOTI var [ENDS] =====


    init {
        db = AgentApp.database
        sdb = db!!.openHelper.writableDatabase
        preferences = Preferences.getInstance()
    }

    // ROOM INSERT, UPDATE, DELETE RULES:
    // INSERT = insert new object - use ROOM (single or bulk insert method)

    // <DISCUSS UPDATE AND DELETE OPTIONS>
    // UPDATE = single update use runintransaction, bulk using custrom query (GeneralUpdateQuery)
    // DELETE = single and bulk delete use runintransaction

    //===== UPDATE DB (SINGLE)
    //update db 1 column
    fun updateDB1Col(tableName: String, cv: ContentValues, col1Name: String, col1Value: String) {
        db!!.runInTransaction {
            sdb.update(tableName, SQLiteDatabase.CONFLICT_IGNORE, cv, String.format("%s = ?", col1Name), arrayOf(col1Value))
        }
    }

    //update db 2 columns
    fun updateDB2Col(tableName: String, cv: ContentValues, col1Name: String, col1Value: String, col2Name: String, col2Value: String) {
        db!!.runInTransaction {
            sdb.update(tableName, SQLiteDatabase.CONFLICT_IGNORE, cv, String.format("%s = ? AND %s = ?", col1Name, col2Name), arrayOf(col1Value, col2Value))
        }
    }

    //===== UPDATE DB (BULK) in GeneralUpdateQuery

    //===== DELETE DB (SINGLE)
    // delete db 1 column
    fun deleteRDB1Col(tableName: String, col1Name: String, col1Value: String) {
        db!!.runInTransaction {
            sdb.delete(tableName, String.format("%s = ?", col1Name), arrayOf(col1Value))
        }
    }

    // delete db 2 columns
    fun deleteRDB2Col(tableName: String, col1Name: String, col1Value: String, col2Name: String, col2Value: String) {
        db!!.runInTransaction {
            sdb.delete(tableName, String.format("%s = ? AND %s = ?", col1Name, col2Name), arrayOf(col1Value, col2Value))
        }
    }

    //===== DELETE DB (BULK)
    // 1 column use query

    fun deleteRDB2ColBulk(tableName: String, col1Name: String, col1ValueList: List<String>, col2Name: String, col2ValueList: List<String>) {
        col1ValueList.forEachIndexed { i, it ->
            // delete first few values first
            sdb.delete(tableName, String.format("%s = ? AND %s = ?", col1Name, col2Name), arrayOf(it, col2ValueList[i]))

            // last item run in transaction to update db
            if (i == col1ValueList.lastIndex) {
                db!!.runInTransaction {
                    sdb.delete(tableName, String.format("%s = ? AND %s = ?", col1Name, col2Name), arrayOf(it, col2ValueList[i]))
                }
            }
        }
    }


    //======== [END] basics ========//


    //======== [START] ContactRoster ========//
    // add new agent to CR
    @Synchronized
    fun checkAndAddtoCR(jid: String, agentName: String?, phoneNumber: String?, profilePicUrl: String?) {
        // add to CR if new
        if (!checkContactExist(jid)) {
            if (profilePicUrl != null && profilePicUrl.isNotEmpty()) {
                // download from url on background thread
                GlobalScope.launch(Dispatchers.IO) {
                    val pic = DownloadProfileFromUrl().getProfile(profilePicUrl)

                    if (!checkContactExist(jid)) {
                        db!!.insertQuery().insertCR(ContactRoster(jid, agentName, phoneNumber, pic))

                    } else {
                        updateCR(jid, agentName, phoneNumber, pic)
                    }
                }
            } else {

                if (!checkContactExist(jid)) {
                    db!!.insertQuery().insertCR(ContactRoster(jid, agentName, phoneNumber))

                } else {
                    updateCR(jid, agentName, phoneNumber, null)
                }
            }
        }
    }

    // update CR
    private fun updateCR(jid: String, agentName: String?, phoneNumber: String?, profileImgBytes: ByteArray?) {
        val cvCR = ContentValues()

        if (agentName != null) {
            cvCR.put(CR_COL_NAME, agentName)
        }
        if (phoneNumber != null) {
            cvCR.put(CR_COL_PHONE, phoneNumber)
        }
        if (profileImgBytes != null) {
            cvCR.put(CR_COL_PROILE_PIC, profileImgBytes)
        }

        updateDB1Col(CR_TABLE_NAME, cvCR, CR_COL_JID, jid)
    }

    // get phone number based on jid
    fun getPhoneNumberCR(jid: String?): String? = db!!.selectQuery().getCRPhoneNumber(jid)


    //======== [END] ContactRoster ========//


    //======== [START] ChatList ========//
    // add new chatroom to chatlist
    @Synchronized
    fun checkAndAddtoCL(jid: String, msgTimeLong: Long, lastDispMsg: String, isOutgoing: Boolean) {
        // add to chatlist if new
        val notiBadge: Int

        if (isOutgoing) {
            notiBadge = 0
        } else {
            notiBadge = getCLNotiBadge(jid) + 1
        }

        if (!checkChatRoomExist(jid)) {
            db!!.insertQuery().insertChatList(ChatList(jid, msgTimeLong, notiBadge, lastDispMsg, 0))

        } else { // update chatlist if NOT new
            val cvCL = ContentValues()

            cvCL.put(CL_COL_DATE, msgTimeLong)

            // only update notiBadge if user is NOT looking at room now
            if (!Preferences.getInstance().getValue("seeing_current_chat").equals(jid)) {
                cvCL.put(CL_COL_NOTI_BADGE, notiBadge)
            }
            cvCL.put(CL_COL_LAST_MSG, lastDispMsg)

            updateDB1Col(CL_TABLE_NAME, cvCL, CL_COL_JID, jid)
        }
    }

    // get number notibadge of a chatroom
    fun getCLNotiBadge(jid: String): Int {
        return db!!.selectQuery().getCLNotiBadge(jid)
    }

    // reset noti badge for a chatroom
    fun clearNotiBadge(jid: String) {
        val cvCL = ContentValues()

        cvCL.put(CL_COL_NOTI_BADGE, 0)

        updateDB1Col(CL_TABLE_NAME, cvCL, CL_COL_JID, jid)
    }

    // delete chat rooms from chatlist
    fun deleteChatRoom(jidList: List<String>) {
        // bulk delete
//        deleteRDB1ColBulk(CL_TABLE_NAME, CL_COL_JID, jidList)

        // bulk delete chat rooms from chatlist
        db!!.deleteQuery().deleteChatListBulk(jidList)
    }

    //======== [END] ChatList ========//


    //======== [START] Message ========//
    // ANY incoming msg (check if agent exists in CR first)
    @Synchronized
    fun checkCRExistsIncoming(jid: String): Boolean {
        // check if need to add to CR
        if (!checkContactExist(jid)) {
            // get agent info from server
            val header = "Bearer " + Preferences.getInstance().accessToken
            val jsonObject = JsonObject()
            jsonObject.addProperty("agent_cea_no", jid)

            val api = RetroAPIClient.api

            try {
                val response = api.getAgentDeatils(jsonObject, header).execute()

                if (!response.isSuccessful) {
                    savePostLogsToDb("inMessage unsuc", response.errorBody().toString(), System.currentTimeMillis())

                    return false
                }

                // add agent dets to CR
                val resp = response.body()
                checkAndAddtoCR(jid, resp?.agentName, resp?.phoneNumber, resp?.s3Path)
                Log.d(TAG, "not exists return true")

                return true

            } catch (e: Exception) {
                savePostLogsToDb("inMessage failure", e.toString(), System.currentTimeMillis())

                return false
            }

        } else { // contact exists, no need add to CR
            Log.d(TAG, "exists return true")
            return true
        }
    }

    fun inTextMsg(jid: String, cTime: Long, stanzaId: String, body: String, msgForward: Int) {
        // check and insert/update to CL
        checkAndAddtoCL(jid, cTime, body, false)

        // insert to msg
        insertNewMsg(jid, Message(jid, 1, cTime, stanzaId, body, 1, 0, null, null, null, null, msgForward))

        // ack msg
        SingleChatStanza().ackStanza(jid, stanzaId)
    }

    fun outMessage(jid: String, body: String, cTime: Long, stanzaId: String, msgForward: Int) {
        // check and insert/update to CL
        checkAndAddtoCL(jid, cTime, body, true)

        // insert to msg
        insertNewMsg(jid, Message(jid, 0, cTime, stanzaId, body, 0, 0, null, null, null, null, msgForward))
    }

    // in/out reply msg
    fun replyMessage(jid: String, body: String, cTime: Long, msgID: String, isSender: Int, replyMsgData: String?,
                     replyMsgID: String, replyImgThumb: String?, replyMsgJid: String) {
        // ack if incoming
        val msgOffline: Int
        val isOutgoing: Boolean
        when (isSender) {
            10, 12 -> { // out
                msgOffline = 0
                isOutgoing = true
            }

            11, 13 -> { // in
                msgOffline = 1
                isOutgoing = false
            }

            else -> {
                msgOffline = 0
                isOutgoing = true
            }
        }

        // check and add to CL
        checkAndAddtoCL(jid, cTime, body, isOutgoing)

        // insert to msg
        insertNewMsg(jid, Message(jid, isSender, cTime, msgID, body, msgOffline, replyMsgData,
                replyMsgID, replyImgThumb, replyMsgJid))

        // ack and add noti badge
        if (!isOutgoing) {
            SingleChatStanza().ackStanza(jid, msgID)
        }
    }

    // out and in delete msg
    fun deleteMessage(context: Context?, jid: String, isSender: Int, delMsgID: String, msgID: String) {
        // update to sqlite
        deleteMsgSqlite(jid, isSender, delMsgID, msgID)

        // delete media + ack if incoming
        if (isSender == 31) {
            // check if file exists before delete
            val delFilePath = getMediaPath(delMsgID)

            if (delFilePath != null) { // if file path null means no such file
                val file = File(delFilePath)
                if (file.exists()) {

                    // check permissions before delete
//                val permStrArr = arrayOf(
//                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//                )
//                if (context != null && PermissionHelper().hasPermissions(context, *permStrArr)) {
                    file.delete()
//                }
                }
            }

            // ack
            SingleChatStanza().ackStanza(jid, msgID)
        }
    }

    // delete msg from sqlite
    private fun deleteMsgSqlite(jid: String, isSender: Int, delMsgID: String, msgID: String) {
        val cvMSG = ContentValues()

        cvMSG.put(MSG_COLUMN_ISSENDER, isSender)
        cvMSG.put(MSG_COLUMN_DATA, GlobalVars.MSG_DELETED)

        // update flagged to 0
        cvMSG.put(MSG_COLUMN_FLAGGED, 0)

        // only update other details if is outgoing
        if (isSender == 30) {
            cvMSG.put(MSG_COLUMN_OFFLINE, 0)

            // update msgID to be deleted into replyMsgID (for sending offline msges)
            cvMSG.put(MSG_COLUMN_REPLY_ID, delMsgID)

            // update uuid to msgID
            cvMSG.put(MSG_COLUMN_ID, msgID)
        }

        updateDB2Col(MSG_TABLE_NAME, cvMSG, MSG_COLUMN_JID, jid, MSG_COLUMN_ID, delMsgID)
    }

    // update offline msg status based on jid + msgID
    fun updateOfflineMsg(jid: String, msgID: String, offlineStatus: Int) {
        val cvMSG = ContentValues()

        cvMSG.put(MSG_COLUMN_OFFLINE, offlineStatus)

        updateDB2Col(MSG_TABLE_NAME, cvMSG, MSG_COLUMN_JID, jid, MSG_COLUMN_ID, msgID)
    }

    // check and send ALL offline msges one shot (used when smack reconnected)
    fun checkAndSendOfflineMsges() {
        if (checkOfflineMsgExist()) { // got offline msges
            // get msg contents
            val offlineMsgList = db!!.selectQuery().getOfflineMsgList()

            // loop each offline msg
            val singleChatStanza = SingleChatStanza()
            offlineMsgList.forEach {
                // send stanza based on isSender
                when (it.IsSender) {
                    0 -> { // text
                        singleChatStanza.NormalChatStanza(it.MsgData, it.MsgJid, Preferences.getInstance().agentName, it.MsgUniqueId)

                    }

                    10 -> { // reply text
                        singleChatStanza.ReplyChatStanza(it.MsgData, it.MsgJid, Preferences.getInstance().agentName, it.MsgUniqueId,
                                it.MsgReplyData, it.MsgReplyUniqueId, "0", it.MsgReplyMediaInfo, it.MsgReplyJid)
                    }

                    12 -> { // reply img
                        singleChatStanza.ReplyChatStanza(it.MsgData, it.MsgJid, Preferences.getInstance().agentName, it.MsgUniqueId,
                                it.MsgReplyData, it.MsgReplyUniqueId, "1", it.MsgReplyMediaInfo, it.MsgReplyJid)
                    }

                    30 -> { // delete msg (delMsgID is stored in replyMsgID col)
                        singleChatStanza.DeleteChatStanza(GlobalVars.MSG_DELETED, it.MsgJid, Preferences.getInstance().agentName,
                                it.MsgUniqueId, it.MsgReplyUniqueId)
                    }
                }

                // msg offline status will be updated by incoming ack stanza
            }
        }
    }

    //--- media: image
    // any outgoing media
    fun outMedia(jid: String, msgID: String, cTime: Long, msgBody: String, filePath: String, fileInfo: String?,
                 fileResID: String?, isSender: Int, msgOffline: Int, msgForward: Int) {
        // check and insert/update to CL
        checkAndAddtoCL(jid, cTime, msgBody, true)

        // insert to msg
        insertMediaToMsg(jid, isSender, cTime, msgID, msgBody, filePath, fileInfo, fileResID, msgOffline, msgForward)
    }

    // any incoming media
    fun inMedia(jid: String, msgID: String, cTime: Long, msgBody: String, filePath: String?, fileInfo: String?,
                fileResID: String?, isSender: Int, msgOffline: Int, msgForward: Int) {
        // check and insert/update to CL
        checkAndAddtoCL(jid, cTime, msgBody, false)

        // insert to msg
        insertMediaToMsg(jid, isSender, cTime, msgID, msgBody, filePath, fileInfo, fileResID, msgOffline, msgForward)

        // enqueue download work
        WorkManagerHelper().enqueueDownloadWork(jid, msgID, fileResID!!, isSender)

        // ack
        SingleChatStanza().ackStanza(jid, msgID)
    }

    // insert new media to Message
    fun insertMediaToMsg(jid: String, isSender: Int, cTime: Long, stanzaId: String, msgBody: String?,
                         filePath: String?, fileInfo: String?, fileResID: String?, msgOffline: Int, msgForward: Int) {
        // insert img to sqlite based on status (msgOffline)
        insertNewMsg(jid, Message(jid, isSender, cTime, stanzaId, msgBody, msgOffline,
                0, filePath, fileInfo, fileResID, null, msgForward))
    }

    // update media row in Message
    fun updateMediaInMsg(jid: String, msgID: String, msgBody: String?, filePath: String?, mediaInfo: String?,
                         mediaResID: String?, msgOffline: Int?, msgWorkID: String?) {
        val cvMSG = ContentValues()

        if (msgBody != null) {
            cvMSG.put(MSG_COLUMN_DATA, msgBody)
        }
        if (filePath != null) {
            cvMSG.put(MSG_COLUMN_PATH, filePath)
        }
        if (mediaInfo != null) {
            cvMSG.put(MSG_COLUMN_MEDIA_INFO, mediaInfo)
        }
        if (mediaResID != null) {
            cvMSG.put(MSG_COLUMN_RES_ID, mediaResID)
        }
        if (msgOffline != null) {
            cvMSG.put(MSG_COLUMN_OFFLINE, msgOffline)
        }
        if (msgWorkID != null) {
            cvMSG.put(MSG_COLUMN_WORK_ID, msgWorkID)
        }

        updateDB2Col(MSG_TABLE_NAME, cvMSG, MSG_COLUMN_JID, jid, MSG_COLUMN_ID, msgID)
    }

    //----- insert queries
    @Synchronized
    private fun insertNewMsg(jid: String, message: Message) {
        // check and insert date item if needed
        checkAndAddDateItem(jid, message.MsgDate)

        db!!.insertQuery().insertMessage(message)
    }
    //----- [END] insert queries

    //----- get queries
    private fun getMediaPath(msgID: String): String? = db!!.selectQuery().getMediaPath(msgID)

    fun getLatestMsgData(jid: String): String? = db!!.selectQuery().getLatestMsgData(jid)

    //----- [END] get queries


    // check and add date item in chat room
    private fun checkAndAddDateItem(jid: String, cTime: Long) {
        // get latest msg of room
        val latestMsgDate = db!!.selectQuery().getLatestMsgDate(jid)

        val calcTime = Calendar.getInstance()

        if (latestMsgDate != null) { // only need check if got msg in room
            // compare with currentTime
            val calLastMsgDate = Calendar.getInstance()

            calLastMsgDate.time = Date(latestMsgDate)
            calcTime.time = Date(cTime)

            val isSameDay = calLastMsgDate.get(Calendar.DAY_OF_YEAR) == calcTime.get(Calendar.DAY_OF_YEAR)
                    && calLastMsgDate.get(Calendar.YEAR) == calcTime.get(Calendar.YEAR)

            if (isSameDay) { // don't add date if same day
                return
            }
        }

        // set date to 00:00 of cTime
        calcTime.set(Calendar.HOUR_OF_DAY, 0) //set hours to zero
        calcTime.set(Calendar.MINUTE, 0) // set minutes to zero
        calcTime.set(Calendar.SECOND, 0) //set seconds to zero
        calcTime.set(Calendar.MILLISECOND, 0)

        // add date item (issender = 40)
        db!!.insertQuery().insertMessage(Message(jid, 40, cTime, UUID.randomUUID().toString(), "",
                1, 0, null, null, null, null, 0))

    }

    //======== [END] Message ========//


    //======== [START] FlagList ========//
    // check and insert msg to flaglist
    fun flagUnflagMsg(msgList: List<Message>, flaggedInt: Int) {
        val jidList = msgList.map {
            it.MsgJid!!
        }
        val msgIDList = msgList.map {
            it.MsgUniqueId!!
        }

        // add date for flagDate if 1
        if (flaggedInt == 1) { // flag
            updateFlagMsgBulk(jidList, msgIDList, System.currentTimeMillis())

        } else { // unflag
            updateFlagMsgBulk(jidList, msgIDList, 0)

        }
    }

    // update msg flag status
    fun updateFlagMsgBulk(jidList: List<String>, msgIDList: List<String>, msgFlagDate: Long) {
        db!!.updateQuery().updateFlagMsgBulk(jidList, msgIDList, msgFlagDate)
    }
    //======== [END] FlagList ========//


    //======== [START] Others ========//
    fun savePostLogsToDb(funcName: String, errorStr: String?, currentTime: Long) {
        Log.e("JAY $funcName", "error = $errorStr")
    }

    fun saveLogsToDb(stanza: String?, err: String?, currentTimeMillis: Long?) {

    }
    //======== [END] Others ========//


    //======== [START] Check-Exists ========//
    // check if contact exists in CR
    @Synchronized
    fun checkContactExist(jid: String?): Boolean {
        return db!!.selectQuery().checkCRJidExist(jid) > 0
    }

    // check if chat room exists in chatlist
    fun checkChatRoomExist(jid: String?): Boolean {
        return db!!.selectQuery().checkChatRoomExist(jid) > 0
    }

    // check if got offline msges in MESSAGE
    fun checkOfflineMsgExist(): Boolean {
        return db!!.selectQuery().checkOfflineMsgExist() > 0
    }

    // check if msg in this room
    fun checkMsgExist(jid: String, msgID: String): Boolean {
        return db!!.selectQuery().checkMsgExist(jid, msgID) > 0
    }
    //======== [END] Check-Exists ========//


    //======== [START] Push Noti ========//

    fun push_go_indi_chat(sender_jid: String, title: String, content: String, senderName: String) {

        val context = AgentApp.instance!!.applicationContext

        var pushChatNotiCount: Int = 0


        var title = title
        val chatRingtone: String
        val savedSoundUri: Uri?
        val vibrate: LongArray?

        chatRingtone = preferences.getValue("ChatRingtone")

        when (chatRingtone) {
            //indi chat ringtone
            "nil" //haven't picked ringtone, use default
            -> {
                savedSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                vibrate = longArrayOf(300, 300)
            }

            "null" //silent
            -> {
                savedSoundUri = null
                vibrate = null
            }

            else -> {
                savedSoundUri = Uri.parse(chatRingtone)
                vibrate = longArrayOf(300, 300)
            }
        }

        pushChatNotiCount++
        val current_room = preferences.getValue("current_room_id")

        if (pushChatNotiCount == 1) { //if only 1 new message
            val intent: Intent
            val pendingIntent: PendingIntent

            prevChatJID = sender_jid

            //get phoneName or displayName + phoneNumber from contact roster
//            val name = getNameFromContactRoster(sender_jid)

            if (senderName != "") {
                title = senderName
            }

            intent = Intent(context, ChatRoom::class.java)

            intent.putExtra("jid", sender_jid)
            intent.putExtra("remoteChat", "1")

            val b = Bundle()
            b.putString("jid", sender_jid)
            b.putString("name", title)
            b.putString("phone", "")
            intent.putExtra("b", b)

            // test 1 - without conditions
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)

            // !!! OLD STRUCTURE
//            if (current_room == null || current_room == "") { //not in an indichatlog
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
//                        intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            } else if (sender_jid == current_room) { //in same indichatlog
//
//                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
//                        intent, PendingIntent.FLAG_ONE_SHOT)
//
//            } else { //in another indichatlog
//
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
//                        intent, PendingIntent.FLAG_ONE_SHOT)
//
//            }

            var titleChat1 = title
            var contentChat1 = content
            val notificationBuilder = NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo))
                    .setColor(context.resources.getColor(R.color.colorPrimary))
                    .setContentTitle(titleChat1)
                    .setContentText(contentChat1)
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushChatNotiCount)
                    .setGroup(sender_jid)
                    .setStyle(NotificationCompat.InboxStyle()
                            .addLine(content)
                            .setBigContentTitle(titleChat1))
                    .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(1, notificationBuilder.build())
        } else { //if more than 1 new messages
            val intent: Intent
            val pendingIntent: PendingIntent
            val notificationBuilder: NotificationCompat.Builder

            title = senderName

            if (sender_jid == prevChatJID) { //current JID same as previous
                intent = Intent(context, ChatRoom::class.java)

                intent.putExtra("jid", sender_jid)
                intent.putExtra("remoteChat", "1")

                val b = Bundle()
                b.putString("jid", sender_jid)
                b.putString("name", title)
                b.putString("phone", "")
                intent.putExtra("b", b)

                // test 1 - without conditions
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT)

//                if (current_room == null || current_room == "") {
//                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
//                            intent, PendingIntent.FLAG_UPDATE_CURRENT)
//                } else if (sender_jid == current_room) { //same jid
//                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
//                            intent, PendingIntent.FLAG_ONE_SHOT)
//
//                } else {
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
//                            intent, PendingIntent.FLAG_ONE_SHOT)
//                }

            } else { //not same JID, go to chattab in home
                intent = Intent(context, HomeActivity::class.java)
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT)
            }

            //outer msg (you have xx messages)
            notificationBuilder = NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo))
                    .setColor(context.resources.getColor(R.color.colorPrimary))
                    .setContentTitle("AgentApp")
                    .setContentText("You have $pushChatNotiCount messages")
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushChatNotiCount)
                    .setGroup(sender_jid)
                    .setContentIntent(pendingIntent)

            //inner msg
            val inboxStyle = NotificationCompat.InboxStyle(notificationBuilder)
            notificationBuilder.setStyle(inboxStyle)
            inboxStyle.setBigContentTitle("AgentApp")

            when (pushChatNotiCount) {
                2 -> {
                    titleChat2 = title
                    contentChat2 = content
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1)
                    inboxStyle.addLine("$title: $content")
                }

                3 -> {
                    titleChat3 = title
                    contentChat3 = content
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1)
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2)
                    inboxStyle.addLine("$title: $content")
                }

                4 -> {
                    titleChat4 = title
                    contentChat4 = content
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1)
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2)
                    inboxStyle.addLine(titleChat3 + ": " + contentChat3)
                    inboxStyle.addLine("$title: $content")
                }

                5 -> {
                    titleChat5 = title
                    contentChat5 = content
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1)
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2)
                    inboxStyle.addLine(titleChat3 + ": " + contentChat3)
                    inboxStyle.addLine(titleChat4 + ": " + contentChat4)
                    inboxStyle.addLine("$title: $content")
                }

                //more than 5 msges
                else -> {
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2)
                    inboxStyle.addLine(titleChat3 + ": " + contentChat3)
                    inboxStyle.addLine(titleChat4 + ": " + contentChat4)
                    inboxStyle.addLine(titleChat5 + ": " + contentChat5)
                    inboxStyle.addLine("$title: $content")
                    inboxStyle.addLine(" ")
                    inboxStyle.addLine("You have $pushChatNotiCount unread messages")

                    titleChat2 = titleChat3
                    titleChat3 = titleChat4
                    titleChat4 = titleChat5
                    titleChat5 = title

                    contentChat2 = contentChat3
                    contentChat3 = contentChat4
                    contentChat4 = contentChat5
                    contentChat5 = content
                }
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notificationBuilder.build())
        }
    }

    fun saveNotiDataToDB(data: FcmMessageModel, apptid: String) {
        db!!.insertQuery().insertPushNoti(PushNotification(data.senderJid, data.title, data.body, "chat", apptid))
    }

    fun deleteNotiFromDB(id: String, isGroup: Int, isAppt: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            db!!.runInTransaction {
                if (isAppt) {
                    sdb.delete("PushNotification", "apptId = ?", arrayOf(id))
                } else {
                    sdb.delete("PushNotification", if (isGroup == 0) "senderJid = ?" else "groupJid = ? ", arrayOf(id))
                }
            }
        }
    }

    //chat
    fun push_bundle_for_NAbove(jid1: String) {
        val context = AgentApp.instance!!.applicationContext

        val notificationManagerCompat = NotificationManagerCompat.from(AgentApp.instance!!.applicationContext)

        var prevChatJID = ""

        val notijidSplit = ArrayList<Int>()
        db!!.selectQuery().getPushMsgs("chat").observeOn(Schedulers.io())
                .flatMap({ pushNotifications: List<PushNotification> ->

                    for (i in 0..pushNotifications.size) {
                        if (i == pushNotifications.size) {
                            notijidSplit.add(i)
                        } else {

                            if (!pushNotifications.get(i).senderJid.equals(prevChatJID)) {
                                notijidSplit.add(i)
                                prevChatJID = pushNotifications.get(i).senderJid
                            } else {
                                prevChatJID = pushNotifications.get(i).senderJid
                            }
                        }
                    }

                    val totalList = ArrayList<List<PushNotification>>()
                    var start = 0
                    var end = 0
                    for (i in notijidSplit.indices) {
                        start = notijidSplit[i]
                        if (notijidSplit.size - 1 == i) {
                            end = start
                        } else {
                            end = notijidSplit[i + 1]
                        }
                        totalList.add(pushNotifications.subList(start, end))
                        val test = pushNotifications.subList(start, end)
                    }
                    Maybe.create<List<List<PushNotification>>> { emitter -> emitter.onSuccess(totalList) }

                })
                .subscribe(object : MaybeObserver<List<List<PushNotification>>?> {
                    override fun onSuccess(lists: List<List<PushNotification>>) {

                        if (lists != null && !lists.isEmpty() && !lists[0].isEmpty()) {
                            val summaryIntent = Intent(context, HomeActivity::class.java)

                            val summaryNotification = NotificationCompat.Builder(context, "ChatMessage")
                                    .setSmallIcon(R.drawable.ic_logo)
                                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo))
                                    .setColor(context.resources.getColor(R.color.colorPrimary))
                                    .setContentTitle("AgentApp Chat")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setGroup("ChatMessage")
                                    .setOnlyAlertOnce(true)
                                    .setGroupSummary(true)
                                    .setAutoCancel(true)
                                    .setLights(Color.BLUE, TimeUnit.SECONDS.toMillis(1).toInt(), TimeUnit.SECONDS.toMillis(1).toInt())
                                    .setContentIntent(PendingIntent.getActivity(context, 1, summaryIntent, PendingIntent.FLAG_UPDATE_CURRENT))

                            val summaryStyle = NotificationCompat.InboxStyle()
                            summaryNotification.setStyle(summaryStyle)

                            val current_room = preferences.getValue("current_room_id")

                            for (list in lists) {

                                val listSize = list.size
                                val lastElement = listSize - 1

                                var jid2 = ""
                                var groupName = ""

                                if (list != null && !list.isEmpty()) {

                                    val intentTarget: Class<*>
                                    val replyIntent = Intent("com.soapp.MsgReply")

                                    jid2 = list[lastElement].senderJid
                                    intentTarget = ChatRoom::class.java
                                    replyIntent.putExtra("isGroup", 0)

                                    val intent = Intent(context, intentTarget)
                                    val jidIntent = jid2

                                    replyIntent.putExtra("jid", jidIntent)
                                    replyIntent.putExtra("type", "reply")

                                    intent.putExtra("jid", jidIntent)
                                    intent.putExtra("remoteChat", "1")

                                    val b = Bundle()
                                    b.putString("jid", jidIntent)
                                    b.putString("name", list.get(0).senderName)
                                    b.putString("phone", "")
                                    intent.putExtra("b", b)

                                    val pendingIntent: PendingIntent

                                    // test 1 - without conditions
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                                            intent, PendingIntent.FLAG_UPDATE_CURRENT)

//                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                                    if (current_room == null || current_room == "") { //not in an indichatlog
//                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                                        pendingIntent = PendingIntent.getActivity(context, jidIntent.hashCode() /* Request code */,
//                                                intent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//                                    } else if (jidIntent == current_room) { //in same indichatlog
//
//                                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//                                        pendingIntent = PendingIntent.getActivity(context, jidIntent.hashCode() /* Request code */,
//                                                intent, PendingIntent.FLAG_ONE_SHOT)
//                                    } else { //in another indichatlog
//                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                                        pendingIntent = PendingIntent.getActivity(context, jidIntent.hashCode() /* Request code */,
//                                                intent, PendingIntent.FLAG_ONE_SHOT)
//                                    }

                                    val remoteInput = RemoteInput.Builder("Reply")
                                            .setLabel("Reply")
                                            .build()

                                    val replyPendingIntent = PendingIntent.getBroadcast(context,
                                            "reply$jidIntent".hashCode(),
                                            replyIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT)

                                    val replyAction = NotificationCompat.Action.Builder(R.drawable.ic_back_arrow_200px,
                                            "Reply", replyPendingIntent)
                                            .addRemoteInput(remoteInput)
                                            .build()

                                    val notificationbuilder = NotificationCompat.Builder(context, "ChatMessage")
                                            .setSmallIcon(R.drawable.ic_logo)
                                            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo))
                                            .setColor(context.resources.getColor(R.color.colorPrimary))
                                            .setContentTitle(list.get(0).senderName)
                                            .setContentText(if (listSize == 1) list[lastElement].pushBody else "$listSize new messages")
                                            .setPriority(if (jidIntent == jid1) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_LOW)
//                                            .setSound(if (jidIntent == jid1) setSoundURIForPush(if (isGroup == 0) "ChatRingtone" else "GrpChatRingtone") else null)
//                                            .setVibrate(if (jidIntent == jid1) setVibrateForPush(if (isGroup == 0) "ChatRingtone" else "GrpChatRingtone") else null)
                                            .setGroup("ChatMessage")
//                                            .addAction(replyAction)
                                            .setAutoCancel(true)
                                            .setContentIntent(pendingIntent)

                                    val inboxStyle = NotificationCompat.InboxStyle()
                                    notificationbuilder.setStyle(inboxStyle)


                                    val lastmsg = if (lastElement - 3 < 0) 0 else lastElement - 3
                                    for (i in lastmsg until listSize) {
                                        val content = list[i]
                                        inboxStyle.addLine(content.pushBody)
                                    }

                                    if (listSize > 4) {
                                        inboxStyle.addLine(String.format("\n %d more messages", listSize - 4))
                                    }

                                    notificationManagerCompat.notify("chat", jidIntent.hashCode(), notificationbuilder.build())
                                    summaryStyle.addLine(list[lastElement].pushBody)
                                }

                                notificationManagerCompat.notify(1, summaryNotification.build())
                            }
                        }
                    }

                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                    }
                })
    }

    //======== [END] Push Noti ========//


    //======== [START] Get from db (object) ========//
    //=== Message table
    fun getMsgObj(jid: String, msgID: String): Message {
        return db!!.selectQuery().getMsgFromJidAndMsgID(jid, msgID)
    }


    //======== [END] Get from db (object) ========//

    /**
     * START APPT RELATED
     */

    @Synchronized
    fun checkApptExist(apptId: String): Boolean {
        return db!!.selectQuery().checkApptExist(apptId) > 0
    }

    // check if appt already expired
    fun checkApptExpired(apptId: String): Boolean {
        return db!!.selectQuery().checkApptExpired(apptId) > 0
    }

    @Synchronized
    fun insertNewAppt(apptDet: CreateApptReq, otherJid: String, statusInt: Int, isOut: Boolean) {
        // only insert note str if is outgoing
        var noteStr: String? = null
        if (isOut) {
            noteStr = apptDet.note
        }
        db!!.insertQuery().insertAppt(Appointment(
                apptDet.appointmentId,
                0,
                0,
                apptDet.subject,
                apptDet.date,
                otherJid,
                apptDet.name,
                apptDet.contactNo,
                apptDet.type,
                apptDet.roomType,
                apptDet.locationName,
                apptDet.locationLat,
                apptDet.locationLon,
                statusInt,
                noteStr,
                if (apptDet.reminder != null && apptDet.reminder.isNotEmpty()) {
                    apptDet.reminder.toLong()
                } else {
                    0
                },
                apptDet.lastUpdatorJid?.toLowerCase() ?: preferences.userXMPPJid
        ))
    }

    fun insertApptToMSg(apptId: String, apptDate: String, otherJid: String, isOut: Boolean, msgDateLong: Long,
                        msgID: String) {
        val msgBody = "Appointment Created"

        // check and add to chatlist first
        checkAndAddtoCL(otherJid, msgDateLong, msgBody, isOut)

        // check and insert date item if needed
        checkAndAddDateItem(otherJid, msgDateLong)

        // insert appt case to msg
        db!!.insertQuery().insertMessage(Message(
                otherJid,
                if (isOut) 50 else 51,
                msgDateLong,
                msgID,
                msgBody,
                apptId,
                ""
        ))
    }

    fun updateApptMsg(apptId: String, otherJid: String, isOut: Boolean, msgDateLong: Long,
                      msgID: String, oldApptDetsJson: String, currentApptDate: Long) {
        // check and add to chatlist first
        val msgBody = "Appointment Updated"
        checkAndAddtoCL(otherJid, msgDateLong, msgBody, isOut)

        // check if updated item is date/time
        val oldApptDet = Gson().fromJson(oldApptDetsJson, Appointment::class.java)
        if (oldApptDet.ApptDateTime != currentApptDate) { // date updated, add to msg
            // check and insert date item if needed
            checkAndAddDateItem(otherJid, msgDateLong)

            db!!.insertQuery().insertMessage(Message(
                    otherJid,
                    if (isOut) 54 else 55,
                    msgDateLong,
                    msgID,
                    msgBody,
                    apptId,
                    oldApptDetsJson
            ))
        }

        // change msg row for original appt creation item
        val delayLong = msgDateLong + 1L
        db!!.updateQuery().updateApptMsg(apptId, delayLong)
    }

    fun updateAppt(apptId: String, date: Long, type: String, roomType: String, note: String?, reminder: Long, lastUpdatorJid: String, status: Int, isOut: Boolean) {
        // only update appt if is outgoing
        if (isOut) {
            db!!.updateQuery().updateAppt(apptId, date, type, roomType, note!!, reminder, lastUpdatorJid, status)
        } else {
            db!!.updateQuery().updateApptNoNote(apptId, date, type, roomType, reminder, lastUpdatorJid, status)
        }
    }

    // update appt dets WITHOUT status
    fun updateApptNoStatus(apptId: String, date: Long, type: String, roomType: String, note: String?, reminder: Long, lastUpdatorJid: String) {
        db!!.updateQuery().updateApptNoStatus(apptId, date, type, roomType, note!!, reminder, lastUpdatorJid)
    }

    fun upateApptStatus(apptId: String, status: Int, isOut: Boolean, msgTime: Long, msgID: String) {
        val apptDetail = getApptDetail(apptId)

        // get last disp msg based on appt status
        val msgBody: String
        val finalStatus: Int
        when (status) {
            3 -> { // rejected/cancelled
                if (isOut) {
                    if (apptDetail.ApptLastUpdatorJid.equals(Preferences.getInstance().userXMPPJid)) { // last was self
                        msgBody = "Appointment Cancelled"
                        finalStatus = 3

                    } else {
                        msgBody = "Appointment Rejected"
                        finalStatus = 4
                    }

                } else {
                    if (apptDetail.ApptLastUpdatorJid.equals(Preferences.getInstance().userXMPPJid)) { // last was self
                        msgBody = "Appointment Rejected"
                        finalStatus = 4
                    } else {
                        msgBody = "Appointment Cancelled"
                        finalStatus = 3
                    }
                }
            }

            2 -> { // pending
                msgBody = "Appointment Pending"
                finalStatus = status
            }

            else -> { // accepted
                msgBody = "Appointment Accepted"
                finalStatus = status

            }
        }
        db!!.updateQuery().updateAppt(apptId, finalStatus)


        // check and add to chatlist first
        val jid = apptDetail.ApptJid!!
        checkAndAddtoCL(jid, msgTime, msgBody, isOut)

        // check and insert date item if needed
        checkAndAddDateItem(jid, msgTime)

        db!!.insertQuery().insertMessage(Message(
                apptDetail.ApptJid,
                if (isOut) 52 else 53,
                msgTime,
                msgID,
                msgBody,
                apptId,
                status.toString()
        ))

    }

    fun getApptWithAgentDetail(apptId: String): ApptWithCR {
        return db!!.selectQuery().getApptWithAgentDetails(apptId)
    }

    fun getApptDetail(apptId: String): Appointment {
        return db!!.selectQuery().getApptDetails(apptId)
    }

    // get appt issender based on apptID
    fun getApptIsOutgoing(apptId: String): Boolean {
        // more than 0 rows returned means is outgoing
        return db!!.selectQuery().getApptOutgoingCount(apptId) > 0
    }

    fun updateApptReminder(apptId: String, milli: Long) {
        Log.d(GlobalVars.I_LOG, "DatabaseHelper updateApptReminder:  $apptId, $milli")
        return db!!.updateQuery().updateApptReminder(apptId, milli)
    }

    fun updateApptToExpired(apptId: String) {
        db!!.updateQuery().updateApptToExpired(apptId)
    }

    //
    fun getApptWorkIDExits(apptID: String): Boolean {
        return db!!.selectQuery().getApptWorkIDExits(apptID)
    }

    fun insertApptWorkId(apptWorkUUID: ApptWorkUUID) {
        db!!.insertQuery().insertApptWorkId(apptWorkUUID)
    }

    fun getApptWorkUUID(apptID: String): ApptWorkUUID? {
        return db!!.selectQuery().getApptWorkUUID(apptID)
    }

    fun getRecentAppttoRemind(endTime: Long): List<Appointment> {
        return db!!.selectQuery().getRecentAppttoRemind(endTime)
    }

    /**
     * END APPT RELATED
     */
}
