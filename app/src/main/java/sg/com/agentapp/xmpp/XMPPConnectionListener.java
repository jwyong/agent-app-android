package sg.com.agentapp.xmpp;

import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

public class XMPPConnectionListener implements ConnectionListener {
    private static final String TAG = "wtf";

    @Override
    public void connected(XMPPConnection connection) {
        Log.d(TAG, "connected() called with: connection = [" + connection + "]");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d(TAG, "authenticated() called with: connection = [" + connection + "], resumed = [" + resumed + "]");
    }

    @Override
    public void connectionClosed() {
        Log.d(TAG, "connectionClosed() called");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(TAG, "connectionClosedOnError() called with: e = [" + e + "]");
    }
}
