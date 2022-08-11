package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName

data class SendFcmToken(

        @field:SerializedName("token_type")
        val tokenType: String? = "fcm",

        @field:SerializedName("token")
        val token: String? = null

) {
    override fun toString(): String {
        return "SendFcmToken(tokenType=$tokenType, token=$token)"
    }
}