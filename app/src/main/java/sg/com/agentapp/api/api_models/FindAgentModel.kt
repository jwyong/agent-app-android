package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName


data class FindAgentModel(

        @field:SerializedName("is_registered")
        val registered: String? = null,

        @field:SerializedName("agent_name")
        val agentName: String? = null,

        @field:SerializedName("phone_number")
        val phoneNumber: String? = null,

        @field:SerializedName("estate_agent_name")
        val estateAgentName: String? = null,

        @field:SerializedName("cea_no")
        val ceaNo: String? = null,

        @field:SerializedName("s3_path")
        var s3Path: String = ""


) {
    override fun toString(): String {
        return "FindAgentModel(registered=$registered, agentName=$agentName, phoneNumber=$phoneNumber, estateAgentName=$estateAgentName, ceaNo=$ceaNo, s3Path=$s3Path)"
    }
}
