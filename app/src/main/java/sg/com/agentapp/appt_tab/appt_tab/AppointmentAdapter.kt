package sg.com.agentapp.appt_tab.appt_tab

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import sg.com.agentapp.R
import sg.com.agentapp.databinding.AppointmentItemWthDateBinding
import sg.com.agentapp.databinding.AppointmentItemWthoutDateBinding
import sg.com.agentapp.sql.entity.Appointment
import java.util.*

class AppointmentAdapter : ListAdapter<Appointment, AppointmentHolder>(DIFF_CALLBACK) {
    // for date title
    private val cal = Calendar.getInstance()
    private val cal2 = Calendar.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentHolder {
        when (viewType) {
            1 -> {
                return AppointmentHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.appointment_item_wthout_date,
                        parent, false) as AppointmentItemWthoutDateBinding, viewType)
            }
            else -> {
                return AppointmentHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.appointment_item_wth_date,
                        parent, false) as AppointmentItemWthDateBinding, viewType)
            }
        }
    }

    override fun onBindViewHolder(holder: AppointmentHolder, position: Int) {
        holder.setData(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        // first item straight got date
        if (position == 0) {
            return 2
        }

        // 2nd item onwards, check dates
        val currentLong = getItem(position).ApptDateTime
        val previousLong = getItem(position - 1).ApptDateTime

        // convert dateTimeLong to dateLong
        cal.timeInMillis = currentLong!!
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val currentDate = cal.timeInMillis

        cal2.timeInMillis = previousLong!!
        cal2.set(Calendar.HOUR, 0)
        cal2.set(Calendar.MINUTE, 0)
        cal2.set(Calendar.SECOND, 0)
        val previousDate = cal2.timeInMillis

        if (currentDate == previousDate) {
            return 1
        } else {
            return 2
        }
    }

    override fun getItemId(position: Int): Long = getItem(position).ApptRow!!.toLong()


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Appointment>() {
            override fun areItemsTheSame(
                    oldUser: Appointment, newUser: Appointment): Boolean {
                return oldUser.ApptId.equals(newUser.ApptId)
            }

            override fun areContentsTheSame(
                    oldUser: Appointment, newUser: Appointment): Boolean {
                return oldUser.equals(newUser)
            }
        }
    }
}
