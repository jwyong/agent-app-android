package sg.com.agentapp.agent_talk.chat_room

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.*
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.agent_details.*
import kotlinx.android.synthetic.main.chat_main.*
import kotlinx.android.synthetic.main.chat_main.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sg.com.agentapp.AgentApp
import sg.com.agentapp.R
import sg.com.agentapp.agent_talk.AgentDetails
import sg.com.agentapp.agent_talk.chat_room.recycler_view.ScrollHelper
import sg.com.agentapp.agent_talk.chat_room.recycler_view.SelectionFuncs
import sg.com.agentapp.appt_tab.AppointmentHelper
import sg.com.agentapp.appt_tab.appt_room.AppointmentChange
import sg.com.agentapp.appt_tab.appt_room.AppointmentDetails
import sg.com.agentapp.appt_tab.appt_room.AppointmentNew
import sg.com.agentapp.databinding.ChatMainBinding
import sg.com.agentapp.global.*
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.entity.Message
import sg.com.agentapp.view_model.ChatRoomMainVM
import sg.com.agentapp.xmpp.outgoing.SingleChatStanza
import java.io.File
import java.util.*

class ChatRoom : BaseActivity() {
    val TAG = "JAY"
    // helpers
    private val uiHelper = UIHelper()

    //===== data binding
    private var binding: ChatMainBinding? = null
    var inputTextLength = ObservableInt()

    // selection observables
    var itemSelected = ObservableInt()
    var showReplyBtn = ObservableBoolean()
    var showDeleteBtn = ObservableBoolean()
    var showCopyBtn = ObservableBoolean()
    var showFlagBtn = ObservableInt() // 1 = flag, 0 = unflag, -1 = hide
    var showForwardBtn = ObservableBoolean()

    // reply observable
    var showReplyUI = ObservableBoolean()
    var replyMsgName = ObservableField<String>()
    var replyMsgImgThumb = ObservableField<String>()
    var replyMsgBody = ObservableField<String>()
    var selectedReplyMsg = Message()

    // recyclerview
    lateinit var chatRoomMainVM: ChatRoomMainVM
    private lateinit var rvLayoutManager: LinearLayoutManager
    var chatRoomAdapter: ChatRoomAdapter = ChatRoomAdapter()

    // agent details
    lateinit var jid: String
    lateinit var name: String
    private lateinit var phone: String
    lateinit var profilePicUrl: String

    // btm msg bar
    private var attachPopupWindow: PopupWindow? = null

    // helpers
    private val btnActionHelper = BtnActionHelper()
    private val permissionHelper = PermissionHelper()
    private val cameraIntent = CameraIntent()
    private val galleryIntent = GalleryIntent()
    private var mediaHelper = MediaHelper()
    private lateinit var audioRecordHelper: AudioRecordHelper
    private lateinit var audioPlaybackHelper: AudioPlaybackHelper
    private lateinit var selectionFuncs: SelectionFuncs
    private lateinit var scrollHelper: ScrollHelper
    private val emojiHelper = EmojiHelper()
    private var singleChatStanza = SingleChatStanza()

    //--- media: audio
    // record
    var isRecording = ObservableBoolean()
    var recordingTimer = ObservableField<String>()

    // playback
    val audioIsPlaying = ObservableBoolean()
    val audioPlaybackMsgID = ObservableField<String>()
    val audioPlaybackStr = ObservableField<String>()

    // activity request codes
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_GALLERY_IMG = 2
    private val REQUEST_GALLERY_DOC = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get intents
        val b = getIntentBundle()

        // forward out msges if got (background)
        if (intent.hasExtra("msgList")) {
            Log.d(TAG, "chatroom got msgList")
            val msgList = intent.extras["msgList"] as List<Message>

            Log.d(TAG, "chatroom msgList = ${msgList[0].MsgData}")

            ForwardHelper(this).forwardMsgList(b, msgList, profilePicUrl)
        }

        // setup data binding
        binding = DataBindingUtil.setContentView(this, R.layout.chat_main)
        binding?.data = this
        binding?.lifecycleOwner = this

        setupToolbar()

        // setup recyclerview functions
        setupRV()
        selectionFuncs = SelectionFuncs(this)
        selectionFuncs.setupRVSelTracker()
        setupLiveData()

        // setup scrolling functions
        scrollHelper = ScrollHelper()
        scrollHelper.setupScrollUX(chat_room_rv, rvLayoutManager)
        scrollHelper.detectKeyboard(binding!!.root)

        setAudioTouchBtnListener()

        // emoji - R&D vanniktech:emoji
        emojiHelper.setupEmojiFunc(relativeChat, msg_input)
    }

    // get intent bundle
    private fun getIntentBundle(): Bundle {
        val b = intent.extras?.getBundle("b")

        jid = b!!.getString("jid")!!.toLowerCase()
        name = b.getString("name")!!
        phone = b.getString("phone")!!

        profilePicUrl = if (b.containsKey("profile_url")) {
            b.getString("profile_url")!!
        } else {
            val byteArray = AgentApp.database!!.selectQuery().getCRProPicByte(jid)

            if (byteArray == null) {
                ""
            } else {
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        }

        return b
    }

    // setup recyclerview
    private fun setupRV() {
        chat_room_rv.apply {
            // set llm details
            rvLayoutManager = LinearLayoutManager(context)
            rvLayoutManager.reverseLayout = true
            rvLayoutManager.stackFromEnd = true
            chat_room_rv.itemAnimator = null

            chatRoomAdapter.setHasStableIds(true)
            // bind to recyclerview
            layoutManager = rvLayoutManager
            adapter = chatRoomAdapter
        }
    }

    // setup recyclerview livedata
    private fun setupLiveData() {
        chatRoomMainVM = ViewModelProviders.of(this).get(ChatRoomMainVM::class.java)
        chatRoomMainVM.loadUsers(jid)
        chatRoomMainVM.messageList?.observe(this, Observer { t ->
            chatRoomAdapter.submitList(t)

            scrollHelper.scrollToBtm()
        })
    }

    //=== for audio recording
    private val audioHandler = Handler()

    // audio record btn listener
    private fun setAudioTouchBtnListener() {
        audioRecordHelper = AudioRecordHelper(this@ChatRoom)
        audioPlaybackHelper = AudioPlaybackHelper(this@ChatRoom)

        audio_record.setOnTouchListener { p0, p1 ->
            when (p1?.action) {
                MotionEvent.ACTION_DOWN -> { // start action ONLY if hold down more than 300ms
                    audioHandler.postDelayed({
                        if (checkPermissions(2)) {
                            // stop any playbacks first
                            clearAllHandlers(1)

                            audioRecordHelper.audioBtnActionDown(p1)
                        }
                    }, 300)

                }

                MotionEvent.ACTION_MOVE -> {
                    // cancel record
                    audioRecordHelper.audioBtnActionMove(p1)

                }

                MotionEvent.ACTION_UP -> {
                    // remove delay handler
                    audioHandler.removeCallbacksAndMessages(null)

                    // get recorded audio file
                    val audioFile = audioRecordHelper.audioBtnActionUp(p1)

                    // send file (upload etc)
                    if (audioFile != null) {
                        mediaHelper.sendMediaFile(this@ChatRoom, audioFile, jid, name,
                                phone, profilePicUrl, 22)
                    }
                }
            }

            true
        }
    }
    //===== END audio record


    //===== for audio playback
    // function for play/pause btn onclick
    fun playPauseBtnOnClick(view: View, msgID: String?, filePath: String?) {
        // check for storage permissions first
        if (checkPermissions(5)) {
            audioPlaybackHelper.playPauseBtnOnClick(view, msgID, filePath)
        }
    }
    //===== END audio playback


    //===== toolbar btns
    //add btn: go to viewing request
    fun createViewReq(_v: View) {
        val intent = Intent(this, AppointmentNew::class.java)
        intent.putExtra("agentid", jid)
        intent.putExtra("agentname", name)
        intent.putExtra("agentphone", phone)
        intent.putExtra("profile_url", profilePicUrl)

        startActivity(intent)
    }

    fun callAgent(_v: View) {
        btnActionHelper.callPhone(this, phone)
    }

    // more btn dropdown
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_room_menu, menu)

        itemSelected.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                menu?.getItem(0)?.isVisible = itemSelected.get() <= 0
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuViewUser -> {
                val intent = Intent(this, AgentDetails::class.java)
                startActivity(intent)
            }

            R.id.menuDeleteChat -> {
                val deleteAction = Runnable {
                    DatabaseHelper.deleteChatRoom(listOf(jid))

                    finish()
                }

                uiHelper.dialog2btn(this, getString(R.string.delete_chatroom_confirm),
                        getString(sg.com.agentapp.R.string.ok), getString(R.string.cancel), deleteAction,
                        null, true)
            }
        }

        return true
    }
    //===== [END] toolbar btns


    //===== selection btn (above toolbar)
    fun replyBtnOnClick(_v: View?) {
        selectionFuncs.replyBtnOnClick()
    }

    fun closeReplyUI(_v: View?) {
        showReplyUI.set(false)
    }

    fun deleteBtnOnClick(_v: View) {
        selectionFuncs.deleteBtnOnClick()
    }

    fun copyBtnOnClick(_v: View) {
        selectionFuncs.copyBtnOnClick()
    }

    fun flagBtnOnClick(_v: View) {
        selectionFuncs.flagBtnOnClick()
    }

    fun forwardBtn(_v: View) {
        selectionFuncs.forwardBtn()
    }

    // appt more btn onclick
    fun moreBtnOnclick(_v: View, apptId: String) {
        // show btm sheet
        val changeFunc = Runnable {
            val intent = Intent(this, AppointmentChange::class.java)
            intent.putExtra("apptId", apptId)
            startActivity(intent)
        }

        val cancelFunc = Runnable {
            val cancelView = Runnable {
                AppointmentHelper(this).acceptRejectFunc(2, apptId, false)
            }
            UIHelper().dialog2btn(this, getString(R.string.cancel_appt_confirm),
                    "YES", "NO", cancelView, null, true)
        }

        uiHelper.btmDialog2Items(this, R.string.change, R.string.cancel_viewing, changeFunc, cancelFunc, true)

    }
    //===== [END] selection btns


    //===== bottom bar btns
    // camera btn
    private val cameraBtnAction = Runnable {
        // check camera permissions
        if (checkPermissions(1)) {
            // launch camera if already got permissions (outgoing img = 0)
            cameraIntent.launchCamera(this, null, REQUEST_IMAGE_CAPTURE, 0, false)
        }
    }

    // gallery (img) btn
    private val imgGalleryBtnAction = Runnable {
        // check media storage permissions
        if (checkPermissions(3)) {
            // launch media picker if already got permissions
            GalleryIntent().launchGallery(this, null, REQUEST_GALLERY_IMG)
        }
    }

    // gallery (files) btn
    private val filePickerBtnAction = Runnable {
        // check media storage permissions
        if (checkPermissions(4)) {
            // launch media picker if already got permissions
            GalleryIntent().launchGallery(this, null, REQUEST_GALLERY_DOC)
        }
    }

    fun attachBtn(_v: View) {
        // close soft keyboard if open
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding?.root?.windowToken, 0)

        // show popup above msg bar
        val msgBarHeight = binding?.root?.msg_bar_main?.height
        val softBtnHeight = uiHelper.getSoftButtonsBarHeight(this)

        attachPopupWindow = uiHelper.popup3Icons(this,
                Gravity.BOTTOM,
                0,
                msgBarHeight!!,
                cameraBtnAction,
                imgGalleryBtnAction,
                filePickerBtnAction
        )
    }

    // send msg btn
    fun sendMsg(_v: View) {
        val msgInputText = msg_input.text.toString().trim()

        if (msgInputText.isNotEmpty()) {
            val uuid = UUID.randomUUID().toString()

            // do send msg function based on reply or normal
            if (showReplyUI.get()) { // reply msg
                sendReplyMsg(msgInputText, uuid)

            } else { // normal text msg
                sendTextMsg(msgInputText, uuid, 0)
            }

            // clear input field
            binding?.msgInput?.setText("")
        }
    }

    // send normal text msg function
    fun sendTextMsg(msgInputText: String, uuid: String, msgForward: Int) {
        if (msgForward == 1) { // forward stanza
            singleChatStanza.ForwardChatStanza(msgInputText, jid, Preferences.getInstance().agentName,
                    uuid, "0", "", "")

        } else { // normal chat stanza
            singleChatStanza.NormalChatStanza(msgInputText, jid, Preferences.getInstance().agentName, uuid)
        }

        // check and insert to CR
        DatabaseHelper.checkAndAddtoCR(jid, name, phone, profilePicUrl)

        // add text msg to sqlite
        DatabaseHelper.outMessage(jid, msgInputText, System.currentTimeMillis(), uuid, msgForward)
    }

    // reply msg send function
    private fun sendReplyMsg(msgInputText: String, uuid: String) {
        // check issender for imgThumb
        var imgThumb = ""
        val replyType: String
        val isSender: Int
        val replyJid: String
        when (selectedReplyMsg.IsSender) {
            20, 21 -> { // img
                imgThumb = selectedReplyMsg.MsgMediaInfo ?: ""
                replyType = "1"
                isSender = 12
            }

            else -> { // text
                replyType = "0"
                isSender = 10
            }
        }

        // for in/out
        when (selectedReplyMsg.IsSender) {
            0, 10, 12, 20, 22, 24 -> { // outgoing - self's jid
                replyJid = Preferences.getInstance().userXMPPJid
            }

            1, 11, 13, 21, 23, 25 -> { // incoming
                replyJid = jid
            }

            else -> {
                replyJid = jid
            }
        }

        // send stanza
        singleChatStanza.ReplyChatStanza(msgInputText, jid, Preferences.getInstance().agentName, uuid,
                selectedReplyMsg.MsgData, selectedReplyMsg.MsgUniqueId, replyType, imgThumb, replyJid)

        // add to sqlite
        DatabaseHelper.replyMessage(jid, msgInputText, System.currentTimeMillis(), uuid, isSender,
                selectedReplyMsg.MsgData, selectedReplyMsg.MsgUniqueId!!, imgThumb, replyJid)

        // clear selection and reply ui
        closeReplyUI(view)
        selectionFuncs.clearSel()

    }

    //===== [END] btm bar btns

    // for audio/send btn change detection
    var watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable) {
            // show reply UI if can reply
            if (!showReplyUI.get()) { // only need show ui if not already shown
                if (selectionFuncs.canReply()) {
                    replyBtnOnClick(null)
                }
            }

            // set text length
            inputTextLength.set(s.length)
        }
    }

    // onclick emoji_btn show/hide
    fun emojiBtn(_v: View) {
        emojiHelper.emojiBtnOnClick(this, msg_input)
    }
    //===== END btm bar btns

    /**
     * Appointment btn click
     */
    fun apptBtnClick(v: View, apptId: String) {
        var positiveFunc: Runnable? = null
        var dialogMsg: String? = null

        when (v.id) {
            R.id.tv_change -> {
                val intent = Intent(this, AppointmentChange::class.java)
                intent.putExtra("apptId", apptId)
                startActivity(intent)
            }
            R.id.tv_accept -> {
                positiveFunc = Runnable {
                    AppointmentHelper(this).acceptRejectFunc(1, apptId, false)
                }
                dialogMsg = getString(R.string.accept_appt_confirm)

            }
            R.id.tv_reject -> {
                positiveFunc = Runnable {
                    AppointmentHelper(this).acceptRejectFunc(2, apptId, false)
                }
                dialogMsg = getString(R.string.reject_appt_confirm)

            }
        }

        if (positiveFunc != null) {
            uiHelper.dialog2btn(this, dialogMsg!!,
                    getString(R.string.yes), getString(R.string.no), positiveFunc,
                    null, true)
        }
    }

    fun apptIvClickToApptDetails(v: View, apptId: String, apptStatus: Int, apptExpiring: Int) {
        if (apptStatus != 3) { // not clickable if cancelled/rejected
            if (apptStatus == 2 && apptExpiring == 1) { // not clickable if pending AND expired
                return
            }
            val intent = Intent(this, AppointmentDetails::class.java)
            intent.putExtra("apptId", apptId)
            startActivity(intent)
        }
    }

    /**
     * END APPT
     */

//--- for camera permissions
// function for checking permissions
    fun checkPermissions(permType: Int): Boolean {
        // declare permissions for camera, storage, etc based on type
        lateinit var permStrArr: Array<String>
        lateinit var permStr: String
        when (permType) {
            1 -> { // camera
                permStrArr = arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                permStr = getString(R.string.perm_camera)
            }

            2 -> { // audio recording
                permStrArr = arrayOf(
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                permStr = getString(R.string.perm_audio)
            }

            3, 4, 5 -> { // media picker (img, docs, etc)
                permStrArr = arrayOf(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                permStr = getString(R.string.perm_gallery)
            }
        }

        // check permissions, ask if any of them not granted
        if (!permissionHelper.hasPermissions(this, *permStrArr)) {
            permissionHelper.askForPermissions(this, null, permStrArr, permType, permStr,
                    null)

            return false
        } else {

            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // prep vars based on requestCode
        val deniedPopupMsg = R.string.perm_failed
        var grantedFun = Runnable {}
        when (requestCode) {
            1 -> { // camera
                grantedFun = cameraBtnAction
            }

            2 -> { // audio
                grantedFun = Runnable {
                    Toast.makeText(this, R.string.perm_audio_granted, Toast.LENGTH_SHORT).show()
                }
            }

            3 -> { // img gallery
                grantedFun = imgGalleryBtnAction
            }

            4 -> { // docs
                grantedFun = filePickerBtnAction
            }

            5 -> { // storage actions (download, play audio, open document, etc)
                // toast user
                grantedFun = Runnable {
                    Toast.makeText(this, R.string.perm_storage_granted, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // show "open settings" popup if denied
        permissionHelper.onPermResults(
                this,
                grantResults,
                deniedPopupMsg,
                grantedFun
        )
    }
//--- END camera permissions

    // for activity results (camera, media picker)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> { // results ok
                when (requestCode) {
                    REQUEST_IMAGE_CAPTURE -> { // camera results
                        GlobalScope.launch(Dispatchers.IO) {
                            val file = cameraIntent.cameraActiResults(this@ChatRoom)

                            // compress (if needed) and send out img
                            mediaHelper.compressAndSendImg(this@ChatRoom, file, jid, name, phone,
                                    profilePicUrl)
                        }
                    }

                    REQUEST_GALLERY_IMG -> { // gallery results (img)
                        GlobalScope.launch(Dispatchers.IO) {
                            // get img uri and convert to file
                            val file = galleryIntent.galleryImgActiResults(this@ChatRoom, data)

                            // compress (if needed) and send out img
                            if (file != null) {
                                mediaHelper.compressAndSendImg(this@ChatRoom, file, jid, name,
                                        phone, profilePicUrl)
                            }
                        }
                    }

                    REQUEST_GALLERY_DOC -> { // gallery results (img)
                        GlobalScope.launch(Dispatchers.IO) {
                            // get document uri and convert to file
                            val file = galleryIntent.galleryDocsActiResults(this@ChatRoom, data)

                            // send out file
                            if (file != null) {
                                mediaHelper.sendMediaFile(this@ChatRoom, file, jid, name,
                                        phone, profilePicUrl, 24)
                            }

                        }
                    }
                }
            }

            Activity.RESULT_CANCELED -> { // results cancelled
                when (requestCode) {
                    REQUEST_IMAGE_CAPTURE -> { // camera - delete temp file
                        val tempFile = File(GlobalVars.IMAGES_PATH_SENT, GlobalVars.TEMP_IMG_NAME)

                        if (tempFile.exists()) {
                            tempFile.delete()
                        }
                    }
                }

            }
        }
    }

    //===== cleanup funcs
    override fun onResume() {
        super.onResume()

        // close any opened popups
        attachPopupWindow?.dismiss()

        // update jid to "seeing current room"
        Preferences.getInstance().save("seeing_current_chat", jid)

        // clear push noti
        val notificationManager = NotificationManagerCompat.from(AgentApp.instance!!.applicationContext)
        notificationManager.cancel(jid.hashCode())
        DatabaseHelper.deleteNotiFromDB(jid, 0, false)

        // clear chat room noti
        GlobalScope.launch(Dispatchers.IO) {
            DatabaseHelper.clearNotiBadge(jid)
        }
    }

    override fun onPause() {
        // clear "seeing current room" jid
        Preferences.getInstance().save("seeing_current_chat", "")

        // clear any handlers
        clearAllHandlers(0)

        super.onPause()
    }

    // clear all record/playback handlers
    fun clearAllHandlers(type: Int) {
        // pause playback
        audioIsPlaying.set(false)
        audioPlaybackHelper.clearHandlers(type)

        // stop recording
        isRecording.set(false)
        recordingTimer.set(null)

        // clear handlers
        audioRecordHelper.clearHandlers()
    }

    override fun onBackPressed() {
        if (showReplyUI.get()) { // reply ui showing, close ui
            showReplyUI.set(false)

        } else if (itemSelected.get() > 0) { // got item selected, just hide UI
            selectionFuncs.clearSel()

        } else { // no items selected, do normal back press action
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        // clear "seeing current room" jid
        Preferences.getInstance().save("seeing_current_chat", "")

        // clear any handlers
        clearAllHandlers(2)

        super.onDestroy()
    }

}