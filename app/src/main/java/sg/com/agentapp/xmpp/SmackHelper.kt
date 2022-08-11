package sg.com.agentapp.xmpp

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.SmackConfiguration
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.debugger.android.AndroidDebugger
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.InternetHelper
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.xmpp.incoming.IncomingStanzaFilter
import sg.com.agentapp.xmpp.incoming.IncomingStanzaListener
import sg.com.agentapp.xmpp.incoming.single.IncomingSingleMsgListener
import java.io.IOException
import java.net.Inet4Address


object SmackHelper {
    private val TAG = "stream"
    private var xmpptcpConnection: XMPPTCPConnection? = null

    // reconnect smack timer
//    private var isConnecting: Boolean = false
//    private var reconnectJob: Job? = null


    init {
        initSmack()
    }

    @Synchronized
    private fun initSmack() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "initSmack: initialised = " + SmackConfiguration.isSmackInitialized())

                val host = Inet4Address.getByName(GlobalVars.XMPP_HOST)
                SmackConfiguration.DEBUG = true
                val builder = XMPPTCPConnectionConfiguration.builder()
                        .setHostAddress(host)
                        .setXmppDomain(GlobalVars.XMPP_DOMAIN)
                        .setPort(GlobalVars.XMPP_PORT)
                        .setResource(GlobalVars.XMPP_RESOURCE)
                        .setUsernameAndPassword(Preferences.getInstance().userXMPPJid, Preferences.getInstance().userXMPPPassword)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                        .enableDefaultDebugger()
                        .setDebuggerFactory(::AndroidDebugger)
                        .setConnectTimeout(10000)

                xmpptcpConnection = XMPPTCPConnection(builder.build())
                xmpptcpConnection!!.addAsyncStanzaListener(IncomingStanzaListener(), IncomingStanzaFilter())
                xmpptcpConnection!!.replyTimeout = 10000

                Roster.setRosterLoadedAtLoginDefault(false)

                ChatManager.getInstanceFor(xmpptcpConnection).addIncomingListener(IncomingSingleMsgListener())

            } catch (e: Exception) {
                //failed to setup stream - jid null
                Log.e(TAG, "initSmack e = ", e)
            }
        }
//        }
    }

    fun getXmpptcpConnection(): XMPPTCPConnection? {
        if (xmpptcpConnection == null) {
            initSmack()

        }

        return xmpptcpConnection
    }

    // function to repeat connect smack
    suspend fun retryConnectSmack(needConnect: Boolean) {
        Log.d(TAG, "smack not empty = " + Preferences.getInstance().userAndPasswordNotEmpty())

        if (connectSmack(needConnect)) {
            InternetHelper.clearReconJob()

            Log.d("smack", "connected, try send offline msges")
            DatabaseHelper.checkAndSendOfflineMsges()

        } else {
            Log.d("smack", "no connected")

            Log.d("smack", "reconnecting in 3 secs...")

            delay(3000)

            retryConnectSmack(needConnect)
        }

    }

    // connect smack single function
    @WorkerThread
    fun connectSmack(needConnect: Boolean): Boolean {
        Log.d(TAG, "connectSmack() isConnected = ${xmpptcpConnection?.isConnected}, isAuth = ${xmpptcpConnection?.isAuthenticated}")
        Log.d(TAG, "connectSmack() username = ${Preferences.getInstance().userXMPPJid}, pass = ${Preferences.getInstance().userXMPPPassword}")
        Log.d(TAG, "connectSmack() connecting...")

        try {
            if (needConnect) {
                xmpptcpConnection?.connect()
            }
            xmpptcpConnection?.login(Preferences.getInstance().userXMPPJid, Preferences.getInstance().userXMPPPassword)

            Log.d(TAG, "connectSmack connected")

            return true

        } catch (e: SmackException) {
            Log.e(TAG, "connectSmack() SmackException", e)

            return false

        } catch (e: IOException) {
            Log.e(TAG, "connectSmack() IOException", e)
            return false

        } catch (e: XMPPException) {
            Log.e(TAG, "connectSmack() XMPPException", e)
            return false

        } catch (e: InterruptedException) {
            Log.e(TAG, "connectSmack() InterruptedException", e)
            return false

        }
    }

    // get stream connection status
    fun getConStatus(): Int {
        if (xmpptcpConnection != null) {
            if (xmpptcpConnection!!.isConnected && xmpptcpConnection!!.isAuthenticated) {
                return 2

            } else if (xmpptcpConnection!!.isConnected) {
                return 1

            }

            // not connected
            return 0
        }

        return 0
    }

    // disconnect smack
    fun disconnectSmack() {
        xmpptcpConnection?.disconnect()
    }
}