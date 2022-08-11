package sg.com.agentapp.global

import android.os.Handler


// class for edit text typing delay ux
class AutoCompHelper(val delayInMillis: Long) {
    private val handler = Handler()

    fun delayAutoComp(text: String, actionFunc: Runnable?, emptyFunc: Runnable?) {
        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({
            if (text.isEmpty()) { // function for when field is empty
                emptyFunc?.run()

            } else { //show search results
                actionFunc?.run()
            }
        }, delayInMillis)
    }
}