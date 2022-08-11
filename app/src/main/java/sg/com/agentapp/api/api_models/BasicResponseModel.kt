package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName


data class BasicResponseModel(

        @field:SerializedName("success")
        val success: String? = null,

        @field:SerializedName("message")
        val message: String? = null
)