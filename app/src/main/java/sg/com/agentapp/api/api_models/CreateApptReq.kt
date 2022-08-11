package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName
import java.util.*

data class CreateApptReq(

        @field:SerializedName("date")
        val date: Long? = null,

        @field:SerializedName("note")
        val note: String = "",

        @field:SerializedName("location_name")
        val locationName: String? = null,

        @field:SerializedName("reminder")
        val reminder: String? = null,

        @field:SerializedName("subject")
        val subject: String? = null,

        @field:SerializedName("contact_no")
        val contactNo: String? = null,

        @field:SerializedName("name")
        val name: String? = null,

        @field:SerializedName("location_lon")
        val locationLon: String? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("location_lat")
        val locationLat: String? = null,

        @field:SerializedName("room_type")
        val roomType: String? = null,

        @field:SerializedName("message_id")
        val messageId: String? = UUID.randomUUID().toString(),

        @field:SerializedName("agent_id")
        val agentId: String? = null,

        @field:SerializedName("resource")
        val resource: String? = "ANDROID",

        @field:SerializedName("appointment_id")
        var appointmentId: String? = null,

        @field:SerializedName("status")
        val status: String? = null,

        @field:SerializedName("last_updator_jid")
        val lastUpdatorJid: String? = null

) {
    override fun toString(): String {
        return "CreateApptReq(date=$date, note=$note, locationName=$locationName, reminder=$reminder, subject=$subject, contactNo=$contactNo, name=$name, locationLon=$locationLon, type=$type, locationLat=$locationLat, roomType=$roomType, messageId=$messageId, agentId=$agentId, resource=$resource, appointmentId=$appointmentId, status=$status, lastUpdatorJid=$lastUpdatorJid)"
    }
}
