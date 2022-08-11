package sg.com.agentapp.sql.joiner

import androidx.room.Embedded
import androidx.room.Relation
import sg.com.agentapp.sql.entity.Appointment
import sg.com.agentapp.sql.entity.ContactRoster

data class ApptWithCR(

        @Embedded
        var appt: Appointment,

        @Relation(parentColumn = "ApptJid", entityColumn = "CRJid")
        var cr: List<ContactRoster>
) {
    override fun toString(): String {
        return "ApptWithCR(appt=$appt, cr=$cr)"
    }
}