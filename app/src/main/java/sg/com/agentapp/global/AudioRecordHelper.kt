package sg.com.agentapp.global

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import java.io.File

class AudioRecordHelper(context: Context) {
    private val TAG = "JAY"
    private var touchXPos = 0F
    private val chatRoom: ChatRoom = context as ChatRoom
    private val ctx = context
    private val mediaHelper = MediaHelper()

    // media recorder
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    // boolean for already cancelled
    private var alreadyCancelled = false

    fun audioBtnActionDown(event: MotionEvent) {
        Log.d(TAG, "start audio")

        // set alreadyCancelled to false (fresh start)
        alreadyCancelled = false

        // save touch position for cancel audio
        touchXPos = event.rawX

        // update UI (boolean)
        updateIsRecording(true)

        // play sound
        ctx.vibrate()

        // start recording
        startRecording()

    }

    fun audioBtnActionMove(event: MotionEvent) {
        if (!alreadyCancelled) { // only do action if NOT already cancelled
            if (event.rawX < (touchXPos - 200)) {
                Log.d(TAG, "cancel audio")

                // update bool to NOT recording
                updateIsRecording(false)

                // stop recording
                stopRecording(true)

                // set alreadyCancelled to true
                alreadyCancelled = true
            }
        }
    }

    fun audioBtnActionUp(event: MotionEvent): File? {
        if (!alreadyCancelled && isRecording) { // only do action if NOT already cancelled & is recording
            // update bool to NOT recording
            updateIsRecording(false)

            if (timerInt > 0) { // send audio if recorded more than 1 sec
                Log.d(TAG, "send audio")

                return stopRecording(false)
            } else { // don't send if less than 1 sec
                Log.d(TAG, "delete audio file")

                return stopRecording(true)
            }
        }

        return null
    }

    // update isRecording in chatroom
    private fun updateIsRecording(isRecording: Boolean) {
        chatRoom.isRecording.set(isRecording)
    }

    // vibrate when start recording
    fun Context.vibrate(milliseconds: Long = 300) {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Check whether device/hardware has a vibrator
        val canVibrate: Boolean = vibrator.hasVibrator()

        if (canVibrate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // void vibrate (VibrationEffect vibe)
                vibrator.vibrate(
                        VibrationEffect.createOneShot(
                                milliseconds,
                                // The default vibration strength of the device.
                                VibrationEffect.DEFAULT_AMPLITUDE
                        )
                )
            } else {
                // This method was deprecated in API level 26
                vibrator.vibrate(milliseconds)
            }
        }
    }

    //===== for timer
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

        chatRoom.recordingTimer.set(timerStr)

        // schedule timer for 1sec later
        timerHandler.postDelayed(timerRunnable, 1000)
        timerInt++
    }

    // stop timer
    private fun stopTimer() {
        // reset timer vals to 0
        timerInt = 0

        timerHandler.removeCallbacks(timerRunnable)
    }

    // clear handlers
    fun clearHandlers() {
        // cancel recording\
        stopRecording(false)

        // stop timer
        stopTimer()
    }

    // setup media recorder if not done yet
    private fun setupRecorder() {
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setAudioEncodingBitRate(28800)
            mediaRecorder?.setAudioSamplingRate(44100)
        }
    }

    // start audio recording
    private fun startRecording() {
        // setup recorder
        setupRecorder()

        // create file first before start record
        audioFile = DirectoryHelper().saveFile(ctx, 22, "mp4")
        mediaRecorder?.setOutputFile(audioFile?.absolutePath)

        // start timer
        startTimer()

        // start recording

        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            isRecording = true

        } catch (e: Exception) {
            Log.e(TAG, "start record exception", e)

            // delete file if got exception
            deleteRecordedFile()

            // clear all UI/timer
            isRecording = false
            stopRecording(true)
            updateIsRecording(false)

            // cancel touch listener
            alreadyCancelled = true
        }
    }

    // stop audio recording
    private fun stopRecording(needDeleteFile: Boolean): File? {
        // stop timer
        stopTimer()

        // stop recording ONLY if is recording
        if (isRecording) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                mediaRecorder?.release()
                mediaRecorder = null

                isRecording = false
            } catch (e: Exception) {
                Log.e(TAG, "stop record exception", e)

                // delete file if got exception
                deleteRecordedFile()

                // clear all UI/timer
                mediaRecorder = null
                isRecording = false
                updateIsRecording(false)

                // cancel touch listener
                alreadyCancelled = true
            }

            if (needDeleteFile) { // dispose of files if cancelled
                deleteRecordedFile()

                return null

            } else { // send files if not cancelled
                // check file size
                Log.d(TAG, "recorded audio size = ${audioFile?.length()}")

                // return recorded file to activity
                return audioFile
            }
        }

        return null
    }

    // dispose file/etc (cancelled)
    private fun deleteRecordedFile() {
//        if (audioFile != null && audioFile!!.exists()) {
//            audioFile?.delete()
//        }
    }
}