package sg.com.agentapp.xmpp.incoming

import org.jivesoftware.smack.filter.StanzaFilter
import org.jivesoftware.smack.packet.Stanza
import org.jxmpp.util.XmppStringUtils
import sg.com.agentapp.sql.DatabaseHelper

// for stanzas without body (chat_ack, composing, etc)
class IncomingStanzaFilter : StanzaFilter {
    val TAG = "jay"

    override fun accept(stanza: Stanza): Boolean {

        for (extension in stanza.extensions) {
            when (extension.namespace) {
                // server ack (offlineStatus = 1)
                "urn:xmpp:chat_ack" -> {
                    val jid = XmppStringUtils.parseLocalpart(stanza.from.toString())
                    val msgID = stanza.stanzaId

                    DatabaseHelper.updateOfflineMsg(jid, msgID, 1)
                }

                // others: composing, etc
            }
        }

        return false
    }
}
