package sg.com.agentapp.sql.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index("chatJid", unique = true)])
class ChatList {

    @PrimaryKey(autoGenerate = true)
    var chatRow: Int? = null
    var chatJid: String? = null
    var chatDate: Long? = 0L
    var chatNotiBadge: Int? = 0
    var chatLastMsg: String? = null
    var chatTyping: Int? = 0

    constructor()

    @Ignore
    constructor(chatJid: String, lastDateReceived: Long?, notiBadge: Int?, lastDispMsg: String,
                typingStatus: Int?) {
        this.chatJid = chatJid
        chatDate = lastDateReceived
        chatNotiBadge = notiBadge
        chatLastMsg = lastDispMsg
        chatTyping = typingStatus
    }
}
