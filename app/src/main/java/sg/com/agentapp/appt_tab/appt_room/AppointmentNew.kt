package sg.com.agentapp.appt_tab.appt_room

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.*
import androidx.databinding.Observable
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import kotlinx.android.synthetic.main.appointment_new.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.CreateApptReq
import sg.com.agentapp.api.api_models.CreateApptRes
import sg.com.agentapp.appt_tab.AppointmentHelper
import sg.com.agentapp.appt_tab.ApptReminderHelper
import sg.com.agentapp.databinding.AppointmentNewBinding
import sg.com.agentapp.global.*
import sg.com.agentapp.one_maps.OneMapsMain
import sg.com.agentapp.sql.DatabaseHelper
import java.util.*

class AppointmentNew : BaseActivity() {
    private val TAG = "jay"

    // helpers
    private var permissionHelper = PermissionHelper()
    private val uiHelper = UIHelper()

    private val mapReqCode: Int = 1000

    // date/time picker
    private var dateTimePicker: SingleDateAndTimePickerDialog? = null
    var obsDateTimeLong = ObservableLong()
    var date: Date? = null

    private var binding: AppointmentNewBinding? = null

    // for asterisk different colour
    val obsLocLabel = ObservableField<SpannableString>()
    val obsDateLabel = ObservableField<SpannableString>()

    // for type btn grp
    val obsSelectedBtnText = ObservableField<String>()

    // observable fields for location
    var mLocationName = ObservableField<String>()

    // enable btn when required fields entered
    val obsIsBtnEnabled = ObservableBoolean()

    val obsRoomTypeInt = ObservableInt()
    var mLocationLat: String = ""
    var mLocationLong: String = ""
    var mSubject: String = ""
    var mName: String = ""
    var mContactNo: String = ""
    var mRoomType: String = ""
    var mReminder: Long = 0
    var mNote: String = ""

    private var agentId: String? = null
    private var agentName: String? = null
    private var agentPhone: String? = null
    private var agentProfileURL: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, sg.com.agentapp.R.layout.appointment_new)
        binding?.data = this
        setupToolbar()

        setupFieldLabels()
        setupFormValidation()
        obsSelectedBtnText.set(getString(sg.com.agentapp.R.string.appt_radio_btn_sale))

        // agent appt - hide name and contact field
        if (intent.hasExtra("agentid")) {
            agentId = intent.getStringExtra("agentid").toLowerCase()
            agentName = intent.getStringExtra("agentname")
            agentPhone = intent.getStringExtra("agentphone")
            agentProfileURL = intent.getStringExtra("profile_url")
            hideViewForAgentType()
        } else {

        }
    }

    // setup field labels with asterisk (red colour)
    private fun setupFieldLabels() {
        // location
        val spannable = SpannableString(getString(sg.com.agentapp.R.string.appt_loc_label))
        val length = spannable.length
        spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                length - 2, length - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        obsLocLabel.set(spannable)

        // date/time
        val spannable2 = SpannableString(getString(sg.com.agentapp.R.string.appt_date_label))
        val length2 = spannable2.length
        spannable2.setSpan(
                ForegroundColorSpan(Color.RED),
                length2 - 2, length2 - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        obsDateLabel.set(spannable2)
    }

    // enable btn only when required fields entered
    private fun setupFormValidation() {
        mLocationName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (obsDateTimeLong.get() != 0L) {
                    obsIsBtnEnabled.set(true)
                } else {
                    obsIsBtnEnabled.set(false)
                }
            }
        })

        obsDateTimeLong.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (mLocationName.get() != null && mLocationName.get()!!.isNotEmpty()) {
                    obsIsBtnEnabled.set(true)
                } else {
                    obsIsBtnEnabled.set(false)
                }
            }
        })
    }

    private fun hideViewForAgentType() {
        tv_name.visibility = GONE
        edttxt_name.visibility = GONE
        tv_contactno.visibility = GONE
        et_contactno.visibility = GONE
    }

    fun genereateApptId(): String {
        return "${Preferences.getInstance().userXMPPJid}$agentId-${System.currentTimeMillis()}"
    }

    // TODO: new way to use retrofit + coroutine
    private fun postToApi(apptReq: CreateApptReq, agentId: String?) {
        Log.i("wtf", "$apptReq: ")
        CoroutineScope(Dispatchers.Main).launch {
            val progressDialog = startProgDialog()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = sendReq(apptReq)
                    if (result.isSuccessful) {
                        // check and insert to CR (outgoing)
                        Log.d(GlobalVars.TAG1, "AppointmentNew, postToApi: agentID = $agentId, apptID = ${apptReq.appointmentId}")
                        DatabaseHelper.checkAndAddtoCR(agentId!!, agentName, agentPhone, agentProfileURL)

                        // for appt table
                        DatabaseHelper.insertNewAppt(apptReq, agentId, 2, true)

                        // for msg table
                        DatabaseHelper.insertApptToMSg(apptReq.appointmentId!!, apptReq.date.toString(),
                                agentId, true, System.currentTimeMillis(), apptReq.messageId!!)

                        ApptReminderHelper().scheduleLocalNotification(apptReq.appointmentId!!)

                        CoroutineScope(Dispatchers.Main).launch {
                            stopProgDialog(progressDialog)
                            Toast.makeText(applicationContext, "New Appointment Created", Toast.LENGTH_SHORT).show()

                            // go to appt det
                            val intent = Intent(this@AppointmentNew, ChatRoom::class.java)

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                            val b = Bundle()
                            b.putString("jid", agentId)
                            b.putString("name", agentName)
                            b.putString("phone", agentPhone)
                            intent.putExtra("b", b)

                            startActivity(intent)

                            setResult(Activity.RESULT_OK)
                            finish()
                        }

                    } else {
                        val errJson = JSONObject(result.errorBody()!!.string())
                        CoroutineScope(Dispatchers.Main).launch {

                            Toast.makeText(applicationContext, "Unsuccessful. $errJson", Toast.LENGTH_SHORT).show()

                            stopProgDialog(progressDialog)
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        stopProgDialog(progressDialog)
                        Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

    }

    private fun startProgDialog(): Dialog = uiHelper.loadingDialog(this, null)
    private fun stopProgDialog(progressDialog: Dialog) {
        CoroutineScope(Dispatchers.Main).launch {
            progressDialog.dismiss()
        }
    }


    private suspend fun sendReq(apptReq: CreateApptReq): Response<CreateApptRes> = RetroAPIClient.api.createAppointment(
            "Bearer " + Preferences.getInstance().accessToken, apptReq
    )

    //===== onclick functions
    fun openOneMap(_v: View) {
        val permStrArr = arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (!permissionHelper.hasPermissions(this, *permStrArr)) {
            permissionHelper.askForPermissions(this, null, permStrArr
                    , 2, getString(sg.com.agentapp.R.string.perm_location),
                    null)
        } else {
            startActivityForResult(Intent(this, OneMapsMain::class.java), mapReqCode)
        }
    }

    // format date
    fun formatDate(dateLong: Long): String {
        return uiHelper.formatDateFromLong(dateLong, "d MMM yyyy (EEE), h:mm a")
    }

    fun showCalendar(_v: View) {
        // hide keyboard first
        uiHelper.hideActiKeyboard(this)

        // show calendar
        dateTimePicker = SingleDateAndTimePickerDialog.Builder(this)
                .mustBeOnFuture()
                .listener { date ->
                    obsDateTimeLong.set(date.time)
                }
                .title("Select Date and Time")
                .build()
        dateTimePicker?.display()
    }

    fun typeBtnsOnClick(v: View) {
        obsSelectedBtnText.set((v as TextView).text.toString())
    }

    fun cancelBtn(_v: View) {
        finish()
    }

    fun sendRequestBtn(_v: View) {
        val apptReq = CreateApptReq(
                locationName = mLocationName.get(),
                locationLat = mLocationLat,
                locationLon = mLocationLong,
                subject = mSubject,
                date = obsDateTimeLong.get(),
                name = mName.toLowerCase(),
                contactNo = mContactNo,
                type = obsSelectedBtnText.get(),
                roomType = mRoomType,
                note = mNote,
                messageId = UUID.randomUUID().toString(),
                agentId = agentId,
                resource = "ANDROID",
                appointmentId = genereateApptId(),
                reminder = mReminder.toString())
        postToApi(apptReq, agentId)
    }

    fun onRoomTypeSpinnerSelect(parent: View, view: View, pos: Int, id: Long) {
        mRoomType = AppointmentHelper(this).getRoomTypePosFromString(pos, this)
    }

    fun onReminderSpinnerSelect(parent: View, view: View, pos: Int, id: Long) {
        mReminder = ApptReminderHelper().getReminderMillFromStringPos(pos, this)
    }
    //===== END onclick funcs

    // request permissions results
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            2 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startActivityForResult(Intent(this, OneMapsMain::class.java), mapReqCode)
                }
                return
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            mapReqCode -> { // on map results

                if (data != null) {
                    mLocationLat = data.getDoubleExtra("lat", 0.0).toString()
                    mLocationLong = data.getDoubleExtra("lng", 0.0).toString()
                    mLocationName.set(data.getStringExtra("bldName").toString())
                }

                Log.i("Wtf", "${mLocationName.get()} : $mLocationLat : $mLocationLong")
            }
        }
    }

    override fun onBackPressed() {
        // if calendar is open, just close it
        if (dateTimePicker != null && dateTimePicker!!.isDisplaying) {
            dateTimePicker!!.close()
        } else {
            super.onBackPressed()
        }
    }
}
