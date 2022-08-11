package sg.com.agentapp.sql.general_queries

import androidx.room.Dao
import androidx.room.Query

/* Created by jay on 01/10/2018. */

@Dao
interface GeneralDeleteQuery {
    //===== CHATLIST =====
    @Query("DELETE FROM ChatList WHERE chatJid IN (:jidList)")
    fun deleteChatListBulk(jidList: List<String>)
}
