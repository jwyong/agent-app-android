package sg.com.agentapp

import android.app.ActivityManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger.addLogAdapter
import org.jivesoftware.smack.android.AndroidSmackInitializer
import sg.com.agentapp.global.ForegrndListener
import sg.com.agentapp.global.InternetHelper
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.sql.AADatabase
import sg.com.agentapp.work_manager.reminder.PeriodicReminderWorker
import java.util.concurrent.TimeUnit


class AgentApp : Application() {
    override fun onCreate() {
        super.onCreate()

        instance = this

        addLogAdapter(AndroidLogAdapter())

        // foreground listener
        ProcessLifecycleOwner.get()
                .lifecycle
                .addObserver(ForegrndListener())

        // initiate internet checking
        InternetHelper.enable(this)

        setupNotification()
        AndroidSmackInitializer.initialize(this)
        Preferences.getInstance()
        setReminderCheckingPeriodically(true)

        setupEmoji()

        // disable for production
//        DebugGhostBridge(this, "AgentAppDB", 1)
    }

    private fun setupEmoji() {
        EmojiCompat.init(BundledEmojiCompatConfig(this))
    }

    internal fun setupNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("debug", "debug", NotificationManager.IMPORTANCE_NONE)
            val notificationChannel1 = NotificationChannel("123", "General", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationChannel2 = NotificationChannel("ChatMessage", "ChatMessage", NotificationManager.IMPORTANCE_DEFAULT)

            val channelAlarm = NotificationChannel("Reminder", "Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT)
            channelAlarm.description = "Receive reminder for appointments on timely manner"

            val notificationManager = getSystemService(NotificationManager::class.java)

            notificationManager!!.createNotificationChannel(notificationChannel)
            notificationManager.createNotificationChannel(notificationChannel1)
            notificationManager.createNotificationChannel(notificationChannel2)
            notificationManager.createNotificationChannel(channelAlarm)

        }
    }

    //    private fun testGodEye() {
//        GodEye.instance().init(this)
//        if (isMainProcess(this)) {//can not install modules in sub process
//            GodEye.instance().install(GodEyeConfig.fromAssets("android-godeye-config/install.config"))
//        }
//
//        GodEye.instance().getModule<Battery>(GodEye.ModuleName.BATTERY).subject().subscribe()
//        GodEye.instance().getModule<Cpu>(GodEye.ModuleName.CPU).subject().subscribe()
//        GodEye.instance().getModule<Crash>(GodEye.ModuleName.CRASH).subject().subscribe()
//        GodEye.instance().getModule<Fps>(GodEye.ModuleName.FPS).subject().subscribe()
//        GodEye.instance().getModule<Heap>(GodEye.ModuleName.HEAP).subject().subscribe()
//        GodEye.instance().getModule<LeakDetector>(GodEye.ModuleName.LEAK).subject().subscribe()
//        GodEye.instance().getModule<Pageload>(GodEye.ModuleName.PAGELOAD).subject().subscribe()
//        GodEye.instance().getModule<Pss>(GodEye.ModuleName.PSS).subject().subscribe()
//        GodEye.instance().getModule<Ram>(GodEye.ModuleName.RAM).subject().subscribe()
//        GodEye.instance().getModule<Sm>(GodEye.ModuleName.SM).subject().subscribe()
//        GodEye.instance().getModule<ThreadDump>(GodEye.ModuleName.THREAD).subject().subscribe()
//        GodEye.instance().getModule<Traffic>(GodEye.ModuleName.TRAFFIC).subject().subscribe()
//
//
//        GodEyeMonitor.work(this)
//    }
    fun setReminderCheckingPeriodically(needAlertDialog: Boolean) {
        val data = Data.Builder()
                .putBoolean("needAlertDialog", needAlertDialog)
                .build()

        val periodicWorkRequest = PeriodicWorkRequest.Builder(PeriodicReminderWorker::class.java, 1,
                TimeUnit.DAYS)
                .addTag("periodicSetReminder")
                .setInputData(data)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork("periodicSetReminder",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest)
    }

    companion object {
        @get:Synchronized
        var instance: AgentApp? = null
            private set

        val database: AADatabase?
            @Synchronized get() = if (instance != null) {
                AADatabase.getInstance(instance!!)
            } else null

        private fun isMainProcess(application: Application): Boolean {
            val pid = android.os.Process.myPid()
            var processName = ""
            val manager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (process in manager.runningAppProcesses) {
                if (process.pid == pid) {
                    processName = process.processName
                }
            }
            return application.packageName == processName
        }
    }

}