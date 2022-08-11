package sg.com.agentapp.appt_tab.appt_room

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.*
import kotlinx.android.synthetic.main.appointment_change.*
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import sg.com.agentapp.AgentApp
import sg.com.agentapp.R
import sg.com.agentapp.appt_tab.AppointmentHelper
import sg.com.agentapp.appt_tab.ApptReminderHelper
import sg.com.agentapp.databinding.AppointmentDetailBinding
import sg.com.agentapp.global.BaseActivity
import sg.com.agentapp.global.BtnActionHelper
import sg.com.agentapp.global.MiscHelper
import sg.com.agentapp.global.UIHelper
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.joiner.ApptWithCR

class AppointmentDetails : BaseActivity() {
    private lateinit var binding: AppointmentDetailBinding

    // helpers
    private val miscHelper = MiscHelper()
    private val btnActionHelper = BtnActionHelper()

    // obs
    val data = ObservableField<ApptWithCR>()

    val obsApptStatus = ObservableInt()
    val obsIsHost = ObservableBoolean(false)


    // reminder spinner and save btn
    var reminderTime: Long = 0
    var canClickSave = false

    var apptId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get apptID from intent
        apptId = intent.getStringExtra("apptId")

        binding = DataBindingUtil.setContentView(this, R.layout.appointment_detail)
        binding.data = this
        setupToolbar()

        // setup obs for data change
        data.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val apptDet = data.get()!!.appt

                // set obs based on new appt details
                obsApptStatus.set(apptDet.ApptStatus!!)
                obsIsHost.set(apptDet.getIsHost())

                val a = ApptReminderHelper().getReminderStringPosFromMilli(apptDet.ApptReminderTime!!, this@AppointmentDetails)
                sp_reminder.setSelection(ApptReminderHelper().getReminderStringPosFromMilli(apptDet.ApptReminderTime!!, this@AppointmentDetails))
            }

        })
    }

    override fun onResume() {
        super.onResume()

        // get apptData from db based on apptID
        data.set(DatabaseHelper.getApptWithAgentDetail(apptId))
    }

    fun assignBtnTextViews(isHost: Boolean, viewId: Int): String {
        val btnSet: Int // 1 = change, cancel viewing, save, 2 = reject, accept, change

        if (isHost) { // host, just btnSet1
            btnSet = 1
        } else { // not host, check status
            if (data.get()?.appt?.ApptStatus == 1) { // accepted, btnSet 1
                btnSet = 1
            } else {
                btnSet = 2
            }
        }

        // return vals
        return if (btnSet == 1) {
            when (viewId) {
                1 -> "Change"
                2 -> "Cancel Viewing"
                3 -> "Save"
                else -> ""
            }
        } else {
            when (viewId) {
                1 -> "Reject"
                2 -> "Accept"
                3 -> "Change"
                else -> ""
            }
        }
    }

    fun btnOnclick(v: View) {
        // disable btns if cancelled or expired
        if (data.get()?.appt?.ApptStatus == 3 || data.get()?.appt?.ApptExpiring == 1) {
            return
        }

        when ((v as TextView).text) {
            "Change" -> {
                val intent = Intent(this, AppointmentChange::class.java)
                intent.putExtra("apptId", apptId)
                startActivity(intent)
            }

            "Cancel Viewing" -> {
                val cancelView = Runnable {
                    AppointmentHelper(this).acceptRejectFunc(2, apptId, true)
                }
                UIHelper().dialog2btn(this, getString(R.string.cancel_appt_confirm),
                        "YES", "NO", cancelView, null, true)
            }

            "Save" -> { // save reminder if got
                if (canClickSave) {
                    DatabaseHelper.updateApptReminder(apptId, reminderTime)

                    // DONT post to server coz only reminder changed
//                    val apptDet = data.get()
//                    AppointmentHelper(this).updateAppointment(CreateApptReq(
//                            appointmentId = apptId,
//                            date = apptDet?.appt?.ApptDateTime,
//                            type = apptDet?.appt?.ApptAppointmentType,
//                            roomType = apptDet?.appt?.ApptRoomType,
//                            note = apptDet?.appt?.ApptNotes,
//                            reminder = reminderTime.toString(),
//                            lastUpdatorJid = Preferences.getInstance().userXMPPJid
//                    ))

                    // toast then finish
                    miscHelper.toastMsg(this, R.string.appt_det_saved, Toast.LENGTH_SHORT)
                    finish()
                }
            }

            "Reject" -> { // reject appt
                val positiveFunc = Runnable {
                    AppointmentHelper(this).acceptRejectFunc(2, apptId, true)
                }
                UIHelper().dialog2btn(this, "Are you sure want to reject this viewing?",
                        "YES", "NO", positiveFunc, null, true)
            }

            "Accept" -> { // accept appt
                val positiveFunc = Runnable {
                    AppointmentHelper(this).acceptRejectFunc(1, apptId, true)
                }
                UIHelper().dialog2btn(this, "Are you sure want to accept this viewing?",
                        "YES", "NO", positiveFunc, null, true)
            }
        }
    }

    fun callAgent(_v: View) {
        btnActionHelper.callPhone(this, data.get()!!.cr[0].CRPhoneNumber)
    }

    fun sendTextMsg(_v: View) {
        btnActionHelper.sendSms(this, data.get()!!.cr[0].CRPhoneNumber, null)
    }

    fun navigate(_v: View) {
        BtnActionHelper().navigate(this, data.get()!!.appt.ApptLatitude, data.get()!!.appt.ApptLongitude)
    }

    // get toolbar bg colour based on apptStatus
    fun getToolbarBgFromStatus(status: Int): Drawable? {
        val context = AgentApp.instance!!.applicationContext

        if (data.get()?.appt?.ApptExpiring == 1) {
            return ContextCompat.getDrawable(context, R.drawable.bg_grad_diag_grey)

        } else {
            return when (status) {
                3 -> { // rejected/cancelled
                    ContextCompat.getDrawable(context, R.drawable.bg_grad_diag_red)
                }
                2 -> { // pending
                    ContextCompat.getDrawable(context, R.drawable.bg_grad_diag_orange)
                }
                else -> { // confirmed
                    ContextCompat.getDrawable(context, R.drawable.bg_grad_diag_green)


                }
            }
        }
    }

    fun getDateTime(apptDateTime: Long): String? {
        val a = ZonedDateTime.ofInstant(Instant.ofEpochMilli(apptDateTime), ZoneId.systemDefault().normalized())
        return a.format(DateTimeFormatter.ofPattern("dd MMM (EEE) | h:mm a"))
    }

    fun onReminderSpinnerSelect(parent: View, view: View, pos: Int, id: Long) {
        // update can click btn
        canClickSave = true
        reminderTime = ApptReminderHelper().getReminderMillFromStringPos(pos, this)
    }
}