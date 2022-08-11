package sg.com.agentapp.sql.general_queries

import androidx.room.Dao
import androidx.room.Insert
import sg.com.agentapp.sql.entity.*

/* Created by jay on 01/10/2018. */

@Dao
interface GeneralInsertQuery {
    //insert new agent to cr
    @Insert
    fun insertCR(contactRoster: ContactRoster): Long

    // insert new chatroom to chatlist
    @Insert
    fun insertChatList(chatList: ChatList): Long

    @Insert
    fun insertMessage(message: Message): Long

    @Insert
    fun insertPushNoti(pushNotification: PushNotification)


    @Insert
    fun insertAppt(appt: Appointment)

    @Insert
    fun insertApptWorkId(apptWorkUUID: ApptWorkUUID)
}
