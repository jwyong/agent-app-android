package sg.com.agentapp.global

import android.content.Context
import android.net.*
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.xmpp.SmackHelper
import sg.com.agentapp.xmpp.outgoing.SingleChatStanza

object InternetHelper : ConnectivityManager.NetworkCallback() {
    private val TAG = "stream"
    lateinit var connectivityManager: ConnectivityManager

    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

    // reconnect stream repeat job
    private var reconnectJob: Job? = null
    var isForegrnd: Boolean? = null

    fun enable(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    override fun onAvailable(network: Network) {//got internet
        super.onAvailable(network)

        Log.d(TAG, "internet onAvailable")

        checkForegrndInt(true, false)
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {//before internet lost
        super.onLosing(network, maxMsToLive)

        Log.d(TAG, "internet onLosing")

    }

    override fun onLost(network: Network) {//after internet lost
        super.onLost(network)

        checkForegrndInt(true, false)

        Log.d(TAG, "internet onLost")
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) { // cahnges of connection
        super.onCapabilitiesChanged(network, networkCapabilities)

        Log.d(TAG, "internet onCapabilitiesChanged = $networkCapabilities")
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) { //
        // changes of linking
        super.onLinkPropertiesChanged(network, linkProperties)

        Log.d(TAG, "internet onLinkPropertiesChanged")

    }

    // check internet then do stream check
    fun checkForegrndInt(needCheckForegrnd: Boolean, needCheckInt: Boolean) {
        Log.d(TAG, "checkForegrndInt")

        if (needCheckForegrnd) { // only check foregrnd if needed
            if (isForegrnd != null && isForegrnd!!) {
                checkInternet(needCheckInt)
            }
        } else { // no need check foreground, straight go in
            checkInternet(needCheckInt)
        }
    }

    // check stream then ping, reconnect if need
    fun checkInternet(needCheckInt: Boolean) {
        if (needCheckInt) {
            if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected) { //internet connected
                checkReconJob()
            }
        } else {
            checkReconJob()
        }
    }

    fun checkReconJob() {
        if (reconnectJob != null && reconnectJob!!.isActive) {
            Log.d(TAG, "job is active")

        } else {
            Log.d(TAG, "job is not active")
            if (Preferences.getInstance().userAndPasswordNotEmpty()) {

                reconnectJob = CoroutineScope(Dispatchers.IO).launch {

                    when (SmackHelper.getConStatus()) {
                        0 -> { // not connected - straight try connect
                            Log.d(TAG, "checkInternet getConStatus 0")

                            // reconnect smack
                            SmackHelper.retryConnectSmack(true)
                        }

                        1 -> { // connected, not auth - only login
                            Log.d(TAG, "checkInternet getConStatus 1")

                            // reconnect smack
                            SmackHelper.retryConnectSmack(false)
                        }

                        2 -> { // auth - ping
                            Log.d(TAG, "checkInternet getConStatus 2")

                            // ping to server if got connection (need timeout)
                            val pingSuccess = SingleChatStanza().PingStanza()

                            if (pingSuccess) { // got stream, send offline msges
                                Log.d(TAG, "checkInternet connected, Send offline msges")

                                DatabaseHelper.checkAndSendOfflineMsges()

                            } else { // no stream, reconnect smack
                                Log.d(TAG, "checkInternet no ping, try connect smack")

                                // disconnect smack first since no ping
                                SmackHelper.disconnectSmack()

                                // reconnect smack
                                SmackHelper.retryConnectSmack(true)
                            }
                        }
                    }
                }
            }
        }
    }

    // clear reconnect job
    fun clearReconJob() {
        Log.d(TAG, "clearReconJob")

        reconnectJob?.cancel()
    }
}