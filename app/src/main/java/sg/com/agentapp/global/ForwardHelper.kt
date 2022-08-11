package sg.com.agentapp.global

import android.content.Context
import android.os.Bundle
import android.util.Log
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.entity.Message
import sg.com.agentapp.xmpp.outgoing.SingleChatStanza
import java.util.*

class ForwardHelper(context: Context) {
    private val TAG = "JAY"
    private val chatRoom = context as ChatRoom
    private val ctx = context

    // forward msg
    fun forwardMsgList(b: Bundle, forwardMsgList: List<Message>, profilePicUrl: String) {
        val jid = b.getString("jid")!!.toLowerCase()
        val name = b.getString("name")!!
        val phone = b.getString("phone")!!

        // loop send out msges based on issender
        val singleChatStanza = SingleChatStanza()
        val imgProcessHelper = ImgProcessHelper()
        forwardMsgList.forEach {
            if (it.IsSender < 20) { // text - send normal stanza
                chatRoom.sendTextMsg(it.MsgData ?: "", UUID.randomUUID().toString(), 1)

            } else { // media - send out resID
                // prep vals based on issender
                var msgType = "1"
                var isSender = 20

                when (it.IsSender) {
                    20, 21 -> { // img
                        msgType = "1"
                        isSender = 20
                    }

                    22, 23 -> { // audio
                        msgType = "2"
                        isSender = 22
                    }

                    24, 25 -> { // doc
                        msgType = "3"
                        isSender = 24
                    }
                }

                Log.d(TAG, "mediaResID = ${it.MsgMediaResID}")

                // send stanza first
                val msgID = UUID.randomUUID().toString()
                singleChatStanza.ForwardChatStanza(it.MsgData, jid, Preferences.getInstance().agentName,
                        msgID, msgType, it.MsgMediaResID, it.MsgMediaInfo)

                // then update db
                // check and insert to CR
                DatabaseHelper.checkAndAddtoCR(jid, name, phone, profilePicUrl)

                // insert to msg
                DatabaseHelper.outMedia(jid, msgID, System.currentTimeMillis(), it.MsgData ?: "",
                        it.MsgMediaPath ?: "", it.MsgMediaInfo, it.MsgMediaResID, isSender,
                        1, 1)
            }
        }
    }
}