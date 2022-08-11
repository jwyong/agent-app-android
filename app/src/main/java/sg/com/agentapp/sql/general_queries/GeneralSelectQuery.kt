package sg.com.agentapp.sql.general_queries

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Maybe
import sg.com.agentapp.sql.entity.Appointment
import sg.com.agentapp.sql.entity.ApptWorkUUID
import sg.com.agentapp.sql.entity.Message
import sg.com.agentapp.sql.entity.PushNotification
import sg.com.agentapp.sql.joiner.ApptWithCR
import sg.com.agentapp.sql.joiner.ChatTabList
import sg.com.agentapp.sql.joiner.FlagTabList

/* Created by jay on 01/10/2018. */

@Dao
interface GeneralSelectQuery {
    //get list of properties
    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    //    @Query("SELECT *" +
    //            "FROM Property " +
    //            "ORDER BY PropertyName")
    //    LiveData<List<Property>> getPropertyListLD();
    //
    //    @Query("SELECT * FROM Unit WHERE UnitPropID  = :pid")
    //    LiveData<List<Unit>> getUnitListLD(String pid);

    //Contact Roster
    //check if jid exists in CR
    @Query("SELECT COUNT() FROM ContactRoster WHERE CRJid = :jid ")
    fun checkCRJidExist(jid: String?): Int

    @Query("select CRProfilePic from ContactRoster where CRJid= :jid ")
    fun getCRProPicByte(jid: String): ByteArray?

    @Query("select CRPhoneNumber from ContactRoster where CRJid= :jid ")
    fun getCRPhoneNumber(jid: String?): String?


    //===== CHATLIST =====
    @Query("SELECT COUNT() FROM ChatList WHERE chatJid = :jid ")
    fun checkChatRoomExist(jid: String?): Int

    @Query("SELECT * FROM ChatList INNER JOIN ContactRoster ON chatJid = CRJid INNER JOIN (SELECT * FROM Message GROUP BY MsgJid ORDER BY MsgDate) ON chatJid = MsgJid ORDER BY chatDate DESC")
    fun getChatList(): LiveData<List<ChatTabList>>

    @Query("SELECT chatNotiBadge FROM ChatList WHERE chatJid = :jid")
    fun getCLNotiBadge(jid: String): Int


    //===== MESSAGE =====
    // get list of msges from room
    @Query("SELECT * FROM MESSAGE WHERE MsgJid = :jid ORDER BY MsgDate DESC, MsgRow DESC ")
    fun getMessageList(jid: String): DataSource.Factory<Int, Message>

    @Query("SELECT * FROM MESSAGE WHERE MsgJid = :jid AND MsgUniqueId = :msgID limit 1")
    fun getMsgFromJidAndMsgID(jid: String, msgID: String): Message

    @Query("SELECT * FROM MESSAGE WHERE MsgWorkID = :workID limit 1")
    fun getMsgFromWorkID(workID: String): Message

    //check if offline msges exist
    @Query("SELECT COUNT() FROM Message WHERE MsgOffline = 0 ")
    fun checkOfflineMsgExist(): Int

    // get list of offline msges
    @Query("SELECT * FROM MESSAGE WHERE MsgOffline = 0 ORDER BY MsgDate")
    fun getOfflineMsgList(): List<Message>

    @Query("SELECT COUNT() FROM Message WHERE MsgJid = :jid AND MsgUniqueId = :msgID ")
    fun checkMsgExist(jid: String, msgID: String): Int

    @Query("SELECT MsgMediaPath FROM Message WHERE MsgUniqueId = :msgID")
    fun getMediaPath(msgID: String): String?

    @Query("SELECT MsgDate FROM Message WHERE MsgJid = :jid ORDER BY MsgDate DESC limit 1")
    fun getLatestMsgDate(jid: String): Long?

    @Query("SELECT MsgData FROM Message WHERE MsgJid = :jid ORDER BY MsgDate DESC limit 1")
    fun getLatestMsgData(jid: String): String?

    //===== PushNotification =====
    @Query("select * from PushNotification where pushType = :type ORDER BY pnRow ASC, senderJid ASC")
    fun getPushMsgs(type: String): Maybe<List<PushNotification>>


    //===== FlagList =====
    @Query("SELECT * FROM Message LEFT OUTER JOIN ContactRoster on MsgJid = CRJid WHERE MsgFlagDate > 0 ORDER BY MsgFlagDate DESC")
    fun getFlagList(): LiveData<List<FlagTabList>>

    /**
     * Appointment
     */
    // ALL appts (no expired)
    @Query("SELECT * FROM Appointment WHERE ApptStatus < 3 AND ApptExpiring != 1 ORDER BY ApptDateTime ASC ")
    fun getAllApptByDateTime(): LiveData<List<Appointment>>

    // pending/confirmed btn (no expired)
    @Query("SELECT * FROM Appointment WHERE ApptStatus=:apptStatus AND ApptExpiring != 1 ORDER BY ApptDateTime ASC ")
    fun getApptByStatus(apptStatus: Int): LiveData<List<Appointment>>

    // calendar click (show expired)
    @Query("SELECT * FROM Appointment WHERE (ApptStatus = 1 OR (ApptStatus = 2 AND ApptExpiring != 1)) AND ApptDateTime BETWEEN :apptDateLong AND :apptDateLong2 ORDER BY ApptDateTime ASC ")
    fun getApptByDate(apptDateLong: Long, apptDateLong2: Long): LiveData<List<Appointment>>

    // calendar + pending/confirmed btns (show expired)
    @Query("SELECT * FROM Appointment WHERE ApptStatus < 3 AND ApptStatus=:apptStatus AND ApptDateTime >= :apptDateLong AND ApptDateTime < :apptDateLong2 ORDER BY ApptDateTime ASC ")
    fun getApptByDateAndStatus(apptStatus: Int, apptDateLong: Long, apptDateLong2: Long): LiveData<List<Appointment>>

    @Query("SELECT COUNT() FROM Appointment where ApptId = :apptId")
    fun checkApptExist(apptId: String): Int

    @Query("SELECT ApptExpiring FROM Appointment where ApptId = :apptId")
    fun checkApptExpired(apptId: String): Int

    @Transaction
    @Query("SELECT * FROM APPOINTMENT where ApptId=:apptId")
    fun getApptWithAgentDetails(apptId: String): ApptWithCR

    @Query("SELECT * FROM APPOINTMENT where ApptId=:apptId")
    fun getApptDetails(apptId: String): Appointment

    @Query("SELECT COUNT() FROM Message where MsgMediaResID=:apptId AND IsSender=50")
    fun getApptOutgoingCount(apptId: String): Int

    @Query("SELECT reminderUUID, exactUUID, deleteUUID FROM APPTWORKUUID WHERE ApptID = :apptID")
    fun getApptWorkUUID(apptID: String): ApptWorkUUID?

    @Query("select exists(select 1 from ApptWorkUUID where ApptID = :apptid)")
    fun getApptWorkIDExits(apptid: String): Boolean

    @Query("SELECT * FROM APPOINTMENT WHERE ApptDateTime <= :endTime ORDER BY ApptDateTime ASC")
    fun getRecentAppttoRemind(endTime: Long?): List<Appointment>
    /**
     * END APPOINTMENT
     */
}