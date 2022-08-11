package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName


data class GetUserProfileResp(

        @field:SerializedName("website_link")
        val websiteLink: String? = null,

        @field:SerializedName("displayname")
        val displayname: String? = null,

        @field:SerializedName("profile_pic")
        val profilePic: String? = null,

        @field:SerializedName("designation")
        val designation: String? = null,

        @field:SerializedName("email")
        val email: String? = null
) {
    override fun toString(): String {
        return "GetUserProfileResp(websiteLink=$websiteLink, displayname=$displayname, profilePic=$profilePic, designation=$designation, email=$email)"
    }
}