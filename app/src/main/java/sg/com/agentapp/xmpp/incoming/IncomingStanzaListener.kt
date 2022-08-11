package sg.com.agentapp.xmpp.incoming

import android.util.Log
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.StanzaListener
import org.jivesoftware.smack.packet.Stanza

class IncomingStanzaListener : StanzaListener {
    val TAG = "jay"

    @Throws(SmackException.NotConnectedException::class, InterruptedException::class, SmackException.NotLoggedInException::class)
    override fun processStanza(packet: Stanza) {
        Log.d(TAG, "IncomingStanzaListener = $packet")
    }
}
