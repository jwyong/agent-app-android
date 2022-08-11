package sg.com.agentapp.appt_tab.appt_room

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import kotlinx.android.synthetic.main.appointment_change.*
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import sg.com.agentapp.AgentApp
import sg.com.agentapp.R
import sg.com.agentapp.api.api_models.CreateApptReq
import sg.com.agentapp.appt_tab.AppointmentHelper
import sg.com.agentapp.appt_tab.ApptReminderHelper
import sg.com.agentapp.databinding.AppointmentChangeBinding
import sg.com.agentapp.global.BaseActivity
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.global.UIHelper
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.joiner.ApptWithCR

class AppointmentChange : BaseActivity() {
    private lateinit var binding: AppointmentChangeBinding

    var isHost = false
    lateinit var data: ApptWithCR
    var reminderTime: Long = 0
    lateinit var roomType: String
    lateinit var apptNote: String
    var dateTimeLong: Long = -10

    // for obs
    val obsSelectedBtnText = ObservableField<String>()
    val obsNeedPost = ObservableBoolean(false)
    private var isFirstEntry = true

    var apptId = ""

    // appt issender for databinding
    var apptIsOutgoing: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get apptID from intent
        apptId = intent.getStringExtra("apptId")

        // get appt data based on apptID
        data = DatabaseHelper.getApptWithAgentDetail(apptId)
        isHost = data.appt.getIsHost()
        dateTimeLong = data.appt.ApptDateTime!!
        apptIsOutgoing = DatabaseHelper.getApptIsOutgoing(apptId)

        // set chosen type
        obsSelectedBtnText.set(data.appt.ApptAppointmentType)

        binding = DataBindingUtil.setContentView(this, R.layout.appointment_change)
        binding.vm = this
        setupToolbar()

        // set selection for reminder
        ApptReminderHelper().getReminderStringPosFromMilli(data.appt.ApptReminderTime!!, this)
        sp_reminder.setSelection(ApptReminderHelper().getReminderStringPosFromMilli(data.appt.ApptReminderTime!!, this))

        // set selection for room type (disable if confirmed)
        if (data.appt.ApptStatus == 1) { // confirmed - disabled
            sp_room.isEnabled = false
            sp_room.isClickable = false
        } else { // pending - enabled
            sp_room.isEnabled = true
            sp_room.isClickable = true
        }
        sp_room.setSelection(AppointmentHelper(this).getRoomTypeStringFromPos(data.appt.ApptRoomType!!, this))

    }

    fun onRoomTypeSpinnerSelect(parent: View, view: View, pos: Int, id: Long) {
        Log.d(GlobalVars.TAG1, "AppointmentChange, onRoomTypeSpinnerSelect: ")

        // set need post to true
        if (isFirstEntry) {
            isFirstEntry = false
        } else {
            obsNeedPost.set(true)
        }

        roomType = AppointmentHelper(this).getRoomTypePosFromString(pos, this)
    }

    fun onReminderSpinnerSelect(parent: View, view: View, pos: Int, id: Long) {
        reminderTime = ApptReminderHelper().getReminderMillFromStringPos(pos, this)
    }

    fun getDateTime(): String? {
        val a = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateTimeLong), ZoneId.systemDefault().normalized())
        return a.format(DateTimeFormatter.ofPattern("dd MMM (EEE) | h:mm a"))
    }

    fun typeBtnsOnClick(v: View) {
        // set need post to true
        obsNeedPost.set(true)

        obsSelectedBtnText.set((v as TextView).text.toString())
    }

    fun cancelBtnOnClick(_v: View) {
        val cancelView = Runnable {
            AppointmentHelper(this).acceptRejectFunc(2, apptId, true)
        }
        UIHelper().dialog2btn(this, getString(R.string.cancel_appt_confirm),
                "YES", "NO", cancelView, null, true)
    }

    fun saveBtnOnClick(_v: View) {
        Log.d(GlobalVars.TAG1, "AppointmentChange, saveBtnOnClick: obsNeedPost = ${obsNeedPost.get()}")

        DatabaseHelper.updateApptReminder(apptId, reminderTime)
        AppointmentHelper(this).updateAppointment(CreateApptReq(
                appointmentId = apptId,
                date = dateTimeLong,
                type = obsSelectedBtnText.get(),
                roomType = roomType,
                note = et_note.text.toString(),
                reminder = reminderTime.toString(),
                lastUpdatorJid = Preferences.getInstance().userXMPPJid
        ), obsNeedPost.get())
    }

    // get toolbar bg colour based on apptStatus
    fun getToolbarBgFromStatus(status: Int): Drawable? {
        val context = AgentApp.instance!!.applicationContext
        return when (status) {
            3 -> {
                ContextCompat.getDrawable(context, R.drawable.bg_grad_diag_red)
            }
            2 -> {
                ContextCompat.getDrawable(context, R.drawable.bg_grad_diag_orange)
            }
            else -> {
                ContextCompat.getDrawable(context, R.drawable.bg_grad_diag_green)
            }
        }
    }

    fun showCalendar(_v: View) {
        // show calendar
        var dateTimePicker = SingleDateAndTimePickerDialog.Builder(this)
                .mustBeOnFuture()
                .listener { date ->
                    // set need post to true (coz changed date/time)
                    obsNeedPost.set(true)

                    dateTimeLong = date.time
                    edt_date.setText(getDateTime())
                }
                .title("Select Date and Time")
                .build()
        dateTimePicker?.display()
    }
}
