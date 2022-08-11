package sg.com.agentapp.xmpp.incoming.single

import android.util.Log
import org.jivesoftware.smack.packet.Message


class IncomingApptStanza {
    val TAG = "JAY"
    fun filterByApptIDType(stanza: Message) {
        val apptIDType = stanza.getExtension("appt_id")

        Log.d(TAG, "apptIDType = $apptIDType")


    }
}