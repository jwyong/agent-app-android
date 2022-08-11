package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName


data class CreateApptRes(

        @field:SerializedName("appointment_id")
        val appointmentId: String? = null
)