package sg.com.agentapp.sql.entity

import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.view.View
import androidx.core.content.ContextCompat
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import sg.com.agentapp.AgentApp
import sg.com.agentapp.R
import sg.com.agentapp.global.BtnActionHelper
import sg.com.agentapp.global.Preferences
import java.util.*

@Entity
class Appointment {

    @PrimaryKey(autoGenerate = true)
    var ApptRow: Int? = null

    var ApptId: String? = null

    //type of appointment 0=self 1=with Agent
    var ApptType: Int? = null

    //int to indicate expiry
    var ApptExpiring: Int? = 0

    var ApptSubject: String? = null

    var ApptDateTime: Long? = 0L

    var ApptJid: String? = null

    var ApptFName: String? = null

    var ApptFContactNo: String? = null

    var ApptAppointmentType: String? = null
    var ApptRoomType: String? = null

    var ApptLocationName: String? = null
    var ApptLatitude: String? = null
    var ApptLongitude: String? = null

    //Sorting: Hosting > Going > Invited/Undecided > Not Going
    //1 = Accepted
    //2 = Pending
    //3 = Cancelled
    //4 = Rejected
    //outgoing default = hosting (0)
    var ApptStatus: Int? = 0

    //appt additional details/description
    var ApptNotes: String? = null

    //reminder period (how many milis before)
    var ApptReminderTime: Long? = -1L
    var ApptLastUpdatorJid: String? = null


    constructor()

    @Ignore
    constructor(ApptId: String?, ApptType: Int?, ApptExpiring: Int?, ApptSubject: String?, ApptDateTime: Long?, ApptJid: String?, ApptFName: String?, ApptFContactNo: String?, ApptAppointmentType: String?, ApptRoomType: String?, ApptLocationName: String?, ApptLatitude: String?, ApptLongitude: String?, ApptStatus: Int?, ApptNotes: String?, ApptReminderTime: Long?, ApptLastUpdatorJid: String?) {
        this.ApptId = ApptId
        this.ApptType = ApptType
        this.ApptExpiring = ApptExpiring
        this.ApptSubject = ApptSubject
        this.ApptDateTime = ApptDateTime
        this.ApptJid = ApptJid
        this.ApptFName = ApptFName
        this.ApptFContactNo = ApptFContactNo
        this.ApptAppointmentType = ApptAppointmentType
        this.ApptRoomType = ApptRoomType
        this.ApptLocationName = ApptLocationName
        this.ApptLatitude = ApptLatitude
        this.ApptLongitude = ApptLongitude
        this.ApptStatus = ApptStatus
        this.ApptNotes = ApptNotes
        this.ApptReminderTime = ApptReminderTime
        this.ApptLastUpdatorJid = ApptLastUpdatorJid
    }

    // onclick funcs
    fun callAgent(v: View, phone: String) {
        BtnActionHelper().callPhone(v.context, phone)
    }

    fun navigate(v: View) {
        BtnActionHelper().navigate(v.context, ApptLatitude, ApptLongitude)
    }


    fun getFormattedTime(l: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = l

        return DateFormat.format("h:mma", cal).toString()
    }

    fun getFormattedDate(l: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = l
        return DateFormat.format("dd MMM, EEE", cal).toString()
    }

    //===== for apptTab bg
    fun getBgFromStatus(status: Int): Drawable? {
        val context = AgentApp.instance!!.applicationContext

        if (ApptExpiring == 1) { // grey
            return context.getDrawable(R.drawable.xml_bg_grey_linear_gradient)

        } else {
            return when (status) {
                3, 4 -> { // cancelled/rejected
                    context.getDrawable(R.drawable.xml_bg_red_linear_gradient)
                }
                2 -> { // pending - pale orange
                    context.getDrawable(R.drawable.xml_bg_orange_linear_gradient)
                }
                else -> { // accepted - green
                    context.getDrawable(R.drawable.xml_bg_green_linear_gradient)

                }
            }
        }
    }

    //===== for appt item (isSender 50, 51)
    fun getChatBgFromStatus(status: Int): Int {
        val context = AgentApp.instance!!.applicationContext

        return when (status) {
            3, 4 -> {
                ContextCompat.getColor(context, R.color.red)
            }
            2 -> {
                if (ApptExpiring == 1) { // expired - grey
                    ContextCompat.getColor(context, R.color.grey5)
                } else {
                    ContextCompat.getColor(context, R.color.orange)
                }
            }
            else -> {
                ContextCompat.getColor(context, R.color.green)
            }
        }
    }

    fun getStatusTextFromStatus(status: Int, isOut: Boolean): String? {
        return when (status) {
            4 -> {
                "Rejected"
            }

            3 -> {
                "Cancelled"
            }
            2 -> {
                if (ApptExpiring == 1) { // expired
                    "Expired"
                } else {
                    "Pending"
                }
            }
            else -> {
                "Accepted"
            }
        }
    }

    //===== for appt status update (isSender 52, 53)
    fun getApptChatBgFromStatus(msgBody: String): Int {
        val context = AgentApp.instance!!.applicationContext
        return when (msgBody) {
            "Appointment Rejected", "Appointment Cancelled" -> {
                ContextCompat.getColor(context, R.color.red)
            }
            "Appointment Pending" -> {
                ContextCompat.getColor(context, R.color.orange)
            }
            else -> {
                ContextCompat.getColor(context, R.color.green)
            }
        }
    }

    fun getIsHost(): Boolean {
        return ApptLastUpdatorJid == Preferences.getInstance().userXMPPJid
    }

    fun getDateTime(): String? {
        val a = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ApptDateTime!!), ZoneId.systemDefault().normalized())
        return a.format(DateTimeFormatter.ofPattern("dd MMM (EEE), h:mm a"))
    }

    // show or hide 3 btns (change, reject, accept - only for non-hosts)
    fun chatBtnVisiblity(status: Int): Int {

        return if (!getIsHost()) {
            if (status == 2) { // pending
                if (ApptExpiring == 1) { // expired, don't show
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
            } else { // not pending - don't show
                View.INVISIBLE
            }
        } else { // self is host, don't show
            View.INVISIBLE
        }
    }

    // show or hide accepted/pending/cancelled status - only for hosts/correct status
    fun chatStatusVisibility(status: Int): Int {
        return if (!getIsHost()) {
            if (status == 2) { // self is NOT host and pending, don't show
                if (ApptExpiring == 1) { // expired, show
                    View.VISIBLE
                } else {
                    View.GONE
                }
            } else { // self is NOT host and NOT pending, show
                View.VISIBLE
            }
        } else { // self is host, sure show
            View.VISIBLE
        }
    }

    // appt more btn visibility (show when accepted or pending)
    fun moreBtnVisibility(status: Int): Int {
        if (ApptExpiring == 1) { // expired, gone
            return View.GONE
        }

        return when (status) {
            1 -> { // accepted, show
                View.VISIBLE
            }

            2 -> { // pending - check host
                if (getIsHost()) { // self is host, show
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }

            else -> {
                View.GONE
            }
        }
    }

    override fun toString(): String {
        return "Appointment(ApptRow=$ApptRow, ApptId=$ApptId, ApptType=$ApptType, ApptExpiring=$ApptExpiring, ApptSubject=$ApptSubject, ApptDateTime=$ApptDateTime, ApptJid=$ApptJid, ApptFName=$ApptFName, ApptFContactNo=$ApptFContactNo, ApptAppointmentType=$ApptAppointmentType, ApptRoomType=$ApptRoomType, ApptLocationName=$ApptLocationName, ApptLatitude=$ApptLatitude, ApptLongitude=$ApptLongitude, ApptStatus=$ApptStatus, ApptNotes=$ApptNotes, ApptReminderTime=$ApptReminderTime, ApptLastUpdatorJid=$ApptLastUpdatorJid)"
    }
}