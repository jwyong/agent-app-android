package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName

data class ApiCheckPhone(

        @field:SerializedName("phone_number")
        val phoneNumber: String? = null,

        @field:SerializedName("message")
        val message: String? = null
) {
    override fun toString(): String {
        return "ApiCheckPhone(phoneNumber=$phoneNumber, message=$message)"
    }
}