package sg.com.agentapp.sql.joiner

import android.graphics.Color
import android.graphics.Typeface
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.room.Embedded
import sg.com.agentapp.global.UIHelper
import sg.com.agentapp.sql.entity.ChatList
import sg.com.agentapp.sql.entity.ContactRoster
import sg.com.agentapp.sql.entity.Message

data class ChatTabList(
        @Embedded
        var chatList: ChatList,
        @Embedded
        var contactRoster: ContactRoster,
        @Embedded
        var message: Message

) {
    constructor() : this(ChatList(), ContactRoster(), Message())

    // get today's time or date
    fun getMsgTimeDate(): String {

        return UIHelper().dateLongToStrToday(chatList.chatDate!!)
    }
}

@BindingAdapter("android:textStyle")
fun setTypeface(v: TextView, needBold: Boolean) {
    if (needBold) {
        v.setTypeface(null, Typeface.BOLD)
        v.setTextColor(Color.BLACK)
    } else {
        v.setTypeface(null, Typeface.NORMAL)
    }
}