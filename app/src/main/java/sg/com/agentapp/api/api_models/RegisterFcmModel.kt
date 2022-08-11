package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName
import sg.com.agentapp.BuildConfig


data class RegisterFcmModel(

        @field:SerializedName("phone_number")
        val phoneNumber: String? = null,

        @field:SerializedName("device_id")
        val deviceId: String? = null,

        @field:SerializedName("app_version")
        val appVersion: String? = BuildConfig.VERSION_NAME,

        @field:SerializedName("device_description")
        val deviceDescription: String? = "android",

        @field:SerializedName("client_secret")
        val clientSecret: String? = "6fd8ph4c05t9v9yh9neu4pgdbbavif6n0zgp6nqd",

        @field:SerializedName("client_id")
        val clientId: String? = "2"
) {
    override fun toString(): String {
        return "RegisterFcmModel(" +
                "phoneNumber=$phoneNumber, " +
                "deviceId=$deviceId, " +
                "appVersion=$appVersion, " +
                "deviceDescription=$deviceDescription, " +
                "clientSecret=$clientSecret, " +
                "clientId=$clientId" +
                ")"
    }
}