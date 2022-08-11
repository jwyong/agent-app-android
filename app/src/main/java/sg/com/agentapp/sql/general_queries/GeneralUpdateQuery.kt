package sg.com.agentapp.sql.general_queries

import androidx.room.Dao
import androidx.room.Query

/* Created by jay on 01/10/2018. */

@Dao
interface GeneralUpdateQuery {

    //===== MESSAGE =====
    @Query("UPDATE Message SET MsgFlagDate = :msgFlagDate WHERE MsgJid IN (:jidList) AND MsgUniqueId IN (:msgIDList)")
    fun updateFlagMsgBulk(jidList: List<String>, msgIDList: List<String>, msgFlagDate: Long)

    //===== PushNotification =====


    //===== FlagList =====


    /**
     * Appointment
     */
//    @Query("UPDATE Appointment SET ApptDateTime  ")
//    fun updateAppt()


    @Query("UPDATE Appointment SET ApptStatus = :status WHERE ApptId=:apptId")
    fun updateAppt(apptId: String, status: Int)

    @Query("UPDATE Appointment SET ApptDateTime =:date , ApptAppointmentType= :type, ApptRoomType=:roomType, ApptNotes=:note , ApptReminderTime= :reminder, ApptStatus = :status, ApptLastUpdatorJid = :lastUpdatorJid WHERE ApptId = :apptId ")
    fun updateAppt(apptId: String, date: Long, type: String, roomType: String, note: String, reminder: Long, lastUpdatorJid: String, status: Int)

    @Query("UPDATE Appointment SET ApptDateTime =:date , ApptAppointmentType= :type, ApptRoomType=:roomType, ApptNotes=:note , ApptReminderTime= :reminder, ApptLastUpdatorJid = :lastUpdatorJid WHERE ApptId = :apptId ")
    fun updateApptNoStatus(apptId: String, date: Long, type: String, roomType: String, note: String, reminder: Long, lastUpdatorJid: String)

    @Query("UPDATE Appointment SET ApptDateTime =:date , ApptAppointmentType= :type, ApptRoomType=:roomType, ApptReminderTime= :reminder, ApptStatus = :status, ApptLastUpdatorJid = :lastUpdatorJid WHERE ApptId = :apptId ")
    fun updateApptNoNote(apptId: String, date: Long, type: String, roomType: String, reminder: Long, lastUpdatorJid: String, status: Int)

    @Query("UPDATE Appointment SET ApptReminderTime = :reminder WHERE ApptId=:apptId")
    fun updateApptReminder(apptId: String, reminder: Long)


    @Query("UPDATE MESSAGE SET MsgDate= :msgDate WHERE MsgMediaResID=:apptId AND (IsSender=50 OR IsSender=51)")
    fun updateApptMsg(apptId: String, msgDate: Long)

    @Query("UPDATE Appointment SET ApptExpiring = 1 WHERE ApptId=:apptId")
    fun updateApptToExpired(apptId: String)

    /**
     * END APPOINTMENT
     */
}