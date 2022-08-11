package sg.com.agentapp.appt_tab.appt_tab

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import sg.com.agentapp.AgentApp
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.sql.entity.Appointment

class AppointmentMainVM : ViewModel() {
    private val observer = Observer<List<Appointment>> { value -> allAppt.setValue(value) }
    var allAppt: MediatorLiveData<List<Appointment>> = MediatorLiveData()

    private var liveData: LiveData<List<Appointment>> = AgentApp.database?.selectQuery()!!.getAllApptByDateTime()

    init {
        allAppt.addSource(liveData, observer)
    }

    fun getAppt(apptStatus: Int, apptDate: Long) {
        Log.d(GlobalVars.TAG1, "AppointmentMainVM, getApptByStatus: ")

        val queryType: Int
        if (apptStatus == -1) { // all status
            if (apptDate == -1L) { // all date
                // all appt
                queryType = 1
            } else { // all status, 1 date
                queryType = 2
            }

        } else { // 1 status
            if (apptDate == -1L) { // all dates, 1 status
                queryType = 3
            } else { // 1 status, 1 date
                queryType = 4
            }
        }
        resetSource(queryType, apptStatus, apptDate)
    }

    private fun resetSource(queryType: Int, apptStatus: Int, apptDate: Long) {
        allAppt.removeSource(liveData)

        Log.d(GlobalVars.TAG1, "AppointmentMainVM, resetSource: queryType = $queryType")

        when (queryType) {
            1 -> { // all appt
                liveData = AgentApp.database?.selectQuery()!!.getAllApptByDateTime()
            }

            2 -> { // all status, one date
                val dateLong2 = apptDate + 24 * 60 * 60 * 1000
                liveData = AgentApp.database?.selectQuery()!!.getApptByDate(apptDate, dateLong2)
            }

            3 -> { // all dates, 1 status
                liveData = AgentApp.database?.selectQuery()!!.getApptByStatus(apptStatus)
            }

            4 -> { // 1 date, 1 status
                val dateLong2 = apptDate + 24 * 60 * 60 * 1000
                liveData = AgentApp.database?.selectQuery()!!.getApptByDateAndStatus(apptStatus, apptDate, dateLong2)
            }
        }
        allAppt.addSource(liveData, observer)
    }
}
