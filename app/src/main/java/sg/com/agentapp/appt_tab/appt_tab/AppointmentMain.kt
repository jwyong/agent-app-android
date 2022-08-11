package sg.com.agentapp.appt_tab.appt_tab

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.*
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import sg.com.agentapp.R
import sg.com.agentapp.databinding.AppointmentMainBinding
import sg.com.agentapp.find_agent.FindAgent
import sg.com.agentapp.global.UIHelper
import sg.com.agentapp.global.collapse_cal.Day
import sg.com.agentapp.global.collapse_cal.widget.CollapsibleCalendar
import sg.com.agentapp.sql.entity.Appointment
import java.util.*


class AppointmentMain : Fragment() {
    // for data binding
    private lateinit var binding: AppointmentMainBinding
    private lateinit var appointmentAdapter: AppointmentAdapter
    private lateinit var apptVM: AppointmentMainVM
    private lateinit var collapsibleCalendar: CollapsibleCalendar

    // obs
    val obsConfApptChecked = ObservableBoolean(false)
    val obsPendApptChecked = ObservableBoolean(false)

    val obsSelApptStatus = ObservableInt(-1) // start with all
    val obsSelApptDate = ObservableLong(-1L) // start with all

//    val obsCalChosenDay = Ob

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.appointment_main, container, false)
        binding.data = this
        val mView = binding.root

        // rv
        setupRV()
        setupLiveData()

        // calendar
        setupCalendar()

        // others
        setupCheckboxObs()

        return mView
    }

    //===== btn onclick funcs
    fun createNewVR(view: View) {
        val ctx = view.context

        // agentBtn - go to find agent
        val agentBtnAction = Runnable {
            val intent = Intent(ctx, FindAgent::class.java)
            intent.putExtra("newAppt", true)
            startActivity(intent)
        }

        // nonAgentBtn - straight go apptNew
        val nonAgentBtnAction = Runnable {
            //            val intent = Intent(ctx, AppointmentNew::class.java)
//            startActivity(intent)
        }

        UIHelper().dialogNewAppt(ctx, true, agentBtnAction, nonAgentBtnAction)
    }

    fun confirmedTextOnclick(_v: View) {
        obsConfApptChecked.set(!obsConfApptChecked.get())
    }

    fun pendingTextOnclick(_v: View) {
        obsPendApptChecked.set(!obsPendApptChecked.get())
    }

    fun resetOnClick(_v: View) {
        collapsibleCalendar.select(Day(0, 0, 0))
        obsSelApptDate.set(-1)
        obsConfApptChecked.set(false)
        obsPendApptChecked.set(false)
    }

    //===== funcs
    private fun setupCheckboxObs() {
        obsConfApptChecked.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (obsConfApptChecked.get()) { // conf checked
                    if (obsPendApptChecked.get()) { // all appts
                        obsSelApptStatus.set(-1)
                    } else { // confirmed only
                        obsSelApptStatus.set(1)
                    }
                } else { // conf not checked
                    if (obsPendApptChecked.get()) { // pending only
                        obsSelApptStatus.set(2)
                    } else { // bot boxed unchecked, all appts
                        obsSelApptStatus.set(-1)
                    }
                }
            }
        })

        obsPendApptChecked.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (obsPendApptChecked.get()) { // pending checked
                    if (obsConfApptChecked.get()) { // all appts
                        obsSelApptStatus.set(-1)
                    } else { // pending only
                        obsSelApptStatus.set(2)
                    }
                } else { // pending not checked
                    if (obsConfApptChecked.get()) { // confirm only
                        obsSelApptStatus.set(1)
                    } else { // bot boxed unchecked, all appts
                        obsSelApptStatus.set(-1)
                    }
                }
            }
        })

        obsSelApptDate.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                // run query
                apptVM.getAppt(obsSelApptStatus.get(), obsSelApptDate.get())
            }
        })

        obsSelApptStatus.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                // run query
                apptVM.getAppt(obsSelApptStatus.get(), obsSelApptDate.get())
            }
        })
    }

    private fun setupRV() {
        binding.recyclerView.apply {
            // set a LinearLayoutManager to handle Android
            // RecyclerView behavior
            layoutManager = LinearLayoutManager(context)

            appointmentAdapter = AppointmentAdapter()
            appointmentAdapter.setHasStableIds(true)

            adapter = appointmentAdapter
        }
    }

    private fun setupLiveData() {
        apptVM = ViewModelProviders.of(this).get(AppointmentMainVM::class.java)
        apptVM.allAppt.observe(this, Observer<List<Appointment>?> { t ->
            appointmentAdapter.submitList(t)

            t?.forEachIndexed { index, appointment ->
                if (DateUtils.isToday(appointment.ApptDateTime!!)) {
                    binding.recyclerView.scrollToPosition(index)
                }
            }
        })
    }

    private fun setupCalendar() {
        collapsibleCalendar = binding.calendarView
        val today = GregorianCalendar()
        collapsibleCalendar.addEventTag(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        today.add(Calendar.DATE, 1)
        collapsibleCalendar.setCalendarListener(object : CollapsibleCalendar.CalendarListener {

            override fun onDaySelect() {
                val selLong = calDayToLong(collapsibleCalendar.selectedDay)
                obsSelApptDate.set(selLong)
            }

            override fun onItemClick(v: View) {
            }

            override fun onDataUpdate() {
            }

            override fun onMonthChange() {
            }

            override fun onWeekChange(position: Int) {
            }
        })
    }

    private fun calDayToLong(calDay: Day): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, calDay.year)
        cal.set(Calendar.MONTH, calDay.month)
        cal.set(Calendar.DAY_OF_MONTH, calDay.day)
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return cal.timeInMillis
    }
}
