package sg.com.agentapp.global

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class ForegrndListener : LifecycleObserver {
    private var TAG = "stream"

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun startSomething() {
        Log.d(TAG, "start")

        // check for internet connection then reconnect/etc
        InternetHelper.checkForegrndInt(false, true)

        InternetHelper.isForegrnd = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stopSomething() {
        Log.d(TAG, "stop")

        InternetHelper.clearReconJob()

        InternetHelper.isForegrnd = false
    }
}
