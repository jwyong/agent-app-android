package sg.com.agentapp.appt_tab.appt_tab

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import sg.com.agentapp.appt_tab.appt_room.AppointmentDetails
import sg.com.agentapp.databinding.AppointmentItemWthDateBinding
import sg.com.agentapp.databinding.AppointmentItemWthoutDateBinding
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.entity.Appointment

class AppointmentHolder : RecyclerView.ViewHolder {

    private lateinit var apptWithoutDateBinding: AppointmentItemWthoutDateBinding
    private lateinit var apptDateBinding: AppointmentItemWthDateBinding
    private val context: Context = itemView.context
    private var viewType: Int = 0

    internal constructor(itemView: View) : super(itemView)

    constructor(inflate: AppointmentItemWthDateBinding, viewType: Int) : super(inflate.root) {
        apptDateBinding = inflate
        this.viewType = viewType

    }

    constructor(inflate: AppointmentItemWthoutDateBinding, viewType: Int) : super(inflate.root) {
        apptWithoutDateBinding = inflate
        this.viewType = viewType
    }

    fun setData(item: Appointment) {
        when (viewType) {
            1 -> {
                apptWithoutDateBinding.bindData = item
                apptWithoutDateBinding.phone = DatabaseHelper.getPhoneNumberCR(item.ApptJid)
            }
            else -> {
                apptDateBinding.bindData = item
                apptDateBinding.phone = DatabaseHelper.getPhoneNumberCR(item.ApptJid)

            }
        }

        itemView.setOnClickListener { view ->

            Log.d(GlobalVars.TAG1, "AppointmentHolder, setData: apptSubj = ${item.ApptSubject}, apptLocName = ${item.ApptLocationName}")

            val intent = Intent(context, AppointmentDetails::class.java)
            var isHost = false
            isHost = item.ApptLastUpdatorJid == Preferences.getInstance().userXMPPJid
            intent.putExtra("isHost", isHost)
            intent.putExtra("apptId", item.ApptId)
            context.startActivity(intent)
        }

    }

}
