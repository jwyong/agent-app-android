package sg.com.agentapp.registration

import android.os.Handler
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt

class RegCountdownTimer(private var countDownInt: Int) {
    val obsTimerInt = ObservableInt()
    val obsIsBtnEnabled = ObservableBoolean()

    private val timerHandler = Handler()
    private val timerRunnable = Runnable {
        countdownTick()
    }

    // start countdown from given time period (in seconds)
    fun startCountdown() {
        // set observables first
        obsTimerInt.set(countDownInt)
        obsIsBtnEnabled.set(false)

        timerHandler.postDelayed(timerRunnable, 1000)
        obsTimerInt.set(obsTimerInt.get() - 1)
    }

    fun countdownTick() {
        obsTimerInt.set(obsTimerInt.get() - 1)

        if (obsTimerInt.get() == 0) { // timer reached 0, stop
            stopCountdown(false)
            return
        }

        timerHandler.postDelayed(timerRunnable, 1000)
    }

    // stop countdown and reset all
    fun stopCountdown(isCancel: Boolean) {
        obsTimerInt.set(0)

        timerHandler.removeCallbacks(timerRunnable)

        if (!isCancel) { // do action if not cancelled
            obsIsBtnEnabled.set(true)
        }
    }
}