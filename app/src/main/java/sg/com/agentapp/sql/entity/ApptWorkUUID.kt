package sg.com.agentapp.sql.entity

import android.content.ContentValues
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class ApptWorkUUID {

    @PrimaryKey(autoGenerate = true)
    var apptWorkRow: Int? = null

    //    @ForeignKey(entity = Appointment.class, parentColumns = "ApptID", childColumns = "ApptID" , onUpdate = ForeignKey.SET_DEFAULT)
    var apptID: String? = null

    var reminderUUID: String? = null
    var exactUUID: String? = null
    var deleteUUID: String? = null


    constructor()

    @Ignore
    constructor(apptID: String, reminderUUID: String, exactUUID: String, deleteUUID: String) {
        this.apptID = apptID
        this.reminderUUID = reminderUUID
        this.exactUUID = exactUUID
        this.deleteUUID = deleteUUID
    }

    companion object {

        fun fromContentValues(values: ContentValues): ApptWorkUUID {
            val workUUID = ApptWorkUUID()

            if (values.containsKey("ApptID")) {
                workUUID.apptID = values.getAsString("ApptID")
            }
            if (values.containsKey("reminderUUID")) {
                workUUID.reminderUUID = values.getAsString("reminderUUID")
            }
            if (values.containsKey("exactUUID")) {
                workUUID.exactUUID = values.getAsString("exactUUID")
            }
            if (values.containsKey("deleteUUID")) {
                workUUID.deleteUUID = values.getAsString("deleteUUID")
            }
            return workUUID
        }
    }

    override fun toString(): String {
        return "ApptWorkUUID(apptWorkRow=$apptWorkRow, apptID=$apptID, reminderUUID=$reminderUUID, exactUUID=$exactUUID, deleteUUID=$deleteUUID)"
    }
}