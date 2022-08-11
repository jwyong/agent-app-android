package sg.com.agentapp.global

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.chat_out_audio.view.*
import sg.com.agentapp.R
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import java.io.File

class AudioPlaybackHelper(context: Context) {
    val TAG = "JAY"

    val mediaHelper = MediaHelper()
    val ctx = context
    val act = context as ChatRoom

    fun playPauseBtnOnClick(view: View, msgID: String?, filePath: String?) {
        Log.d(TAG, "state = ${view.audio_playback_btn.drawable.constantState}")
        Log.d(TAG, "drawable play = ${view.context.getDrawable(R.drawable.ic_play_button_black)}")
        Log.d(TAG, "drawable pause = ${view.context.getDrawable(R.drawable.ic_video_pause_gradient_150px)}")

        // toast file not exists
        if (!File(filePath).exists()) {
            Toast.makeText(ctx, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
            return
        }

        if (checkImgDrawable(view.audio_playback_btn, R.drawable.ic_play_button_black)) { // play action
            Log.d(TAG, "play")

            // check if playing previously played file
            var needReset = false
            if (act.audioPlaybackMsgID.get() != msgID) { // playing new file, reset all
                Log.d(TAG, "play")

                // stop previous playback
                clearHandlers(1)
                timerInt = 0

                // play new file so need reset
                needReset = true
            }

            // set playing to UI
            setAudioObsr(true, msgID)

            // start playback timer
            startTimer()

            // start playback
            startPlayback(filePath, needReset)

        } else { // pause action
            Log.d(TAG, "pause")

            // set paused to UI
            setAudioObsr(false, msgID)

            // pause playback timer
            pauseTimer(false)

            // pause playback
            pausePlayback()
        }
    }


    //===== for playback function
    private val mediaPlayer = MediaPlayer()

    private fun startPlayback(filePath: String?, needReset: Boolean) {
        // request audio focus first
//        ctx.request

        // set new playback file path if needed
        if (needReset) {
            // stop playback first if need reset
            stopPlayback()

            // then set new filepath

            mediaPlayer.setDataSource(filePath)

            mediaPlayer.prepare()
        }
        mediaPlayer.start()

        // set playback completion
        mediaPlayer.setOnCompletionListener {
            Log.d(TAG, "playback complete")

            // stop playback
            stopPlayback()

            // stop timer
            pauseTimer(true)

            // update UI
            act.audioPlaybackStr.set(null)
            act.audioPlaybackMsgID.set(null)
            act.audioIsPlaying.set(false)
        }
    }

    private fun pausePlayback() {
        mediaPlayer.pause()
    }

    // stop playback completely
    private fun stopPlayback() {
        Log.d(TAG, "stop")

        mediaPlayer.stop()
        mediaPlayer.reset()
    }

    // release media player (kill)
    fun releasePlayback() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }


    //===== for playback timer
    private var timerInt: Int = 0
    private var timerStr: String? = "00:00"

    private val timerHandler = Handler()
    private val timerRunnable = Runnable {
        startTimer()
    }

    // start timer ticking
    fun startTimer() {
        // get timer str in "mm:ss" and set to timer (update UI)
        timerStr = mediaHelper.timeIntToStr(timerInt, false)
        act.audioPlaybackStr.set(timerStr)

        // schedule timer for 1sec later
        timerHandler.postDelayed(timerRunnable, 1000)
        timerInt++
    }

    // stop timer
    private fun pauseTimer(isStop: Boolean) {
        if (isStop) { // stop - clear audio playback time
            timerInt = 0
        }

        // remove timer callback (pause)
        clearHandlers(0)

    }

    // clear handlers
    fun clearHandlers(type: Int) {
        when (type) {
            0 -> { // pause only
                pausePlayback()
            }

            1 -> { // stop
                stopPlayback()
            }

            2 -> { // release
                releasePlayback()
            }
        }

        // stop timer
        timerHandler.removeCallbacks(timerRunnable)
    }

    // check img resource drawable
    fun checkImgDrawable(imageView: ImageView?, imgRes: Int): Boolean {
//        if (ctx != null && imageView != null && imageView.getDrawable() != null) {

        val constantState: Drawable.ConstantState?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            constantState = ctx.resources.getDrawable(imgRes, ctx.theme).constantState
        } else {
            constantState = ctx.resources.getDrawable(imgRes).constantState
        }

        return imageView?.drawable?.constantState == constantState
    }

    // set observables according to play/pause
    private fun setAudioObsr(actionIsPlay: Boolean, msgID: String?) {
        act.audioIsPlaying.set(actionIsPlay)
        act.audioPlaybackMsgID.set(msgID)
    }
}