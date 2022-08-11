package sg.com.agentapp.xmpp.incoming.single

import android.util.Log
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.util.XmppStringUtils
import sg.com.agentapp.sql.DatabaseHelper

// any incoming single msg
class IncomingSingleMsgListener : IncomingChatMessageListener {
    internal var choices = arrayOf("yes", "no", "maybe")
    val TAG = "jay"

    override fun newIncomingMessage(from: EntityBareJid, message: Message, chat: Chat) {
        Log.d(TAG, "IncomingSingleMsgListener = $message")

        // start filter by thread (chat, appt, etc)
        val msgThread = message.thread.toString()

//        GlobalScope.launch(Dispatchers.IO) {
        // basic vals
        val jid = XmppStringUtils.parseLocalpart(message.from.toString())

        // add to CR if not exist
        val checkingBoolean = DatabaseHelper.checkCRExistsIncoming(jid)

        // only proceed if retro passed (true)
        if (checkingBoolean) {
            when (msgThread) {
                "chat" -> { // chat stanzas (text, img, audio, etc)

                    IncomingChatStanza().filterByChatIDType(message)
                }

                "appt" -> { // appt stanzas (new appt, appt accepted/rejected, appt time changed, etc)
                    // do appt exist check

                    IncomingApptStanza().filterByApptIDType(message)
                }

                else -> { // not listed
                    Log.d(TAG, "new incoming stanza NEW THREAD")
                }
            }
        }
//        }
    }
}
