package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName

data class RegResModel(

        @field:SerializedName("access_token")
        val accessToken: String? = null,

        @field:SerializedName("license_no")
        val licenseNo: String? = null,

        @field:SerializedName("ejabberd_password")
        val ejabberdPassword: String? = null,

        @field:SerializedName("name")
        val name: String? = null,

        @field:SerializedName("active_date")
        val activeDate: String? = null,

        @field:SerializedName("estate_agent")
        val estateAgent: String? = null,

        @field:SerializedName("cea_no")
        val ceaNo: String? = null,

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
        return "RegResModel(accessToken=$accessToken, licenseNo=$licenseNo, ejabberdPassword=$ejabberdPassword, name=$name, activeDate=$activeDate, estateAgent=$estateAgent, ceaNo=$ceaNo, websiteLink=$websiteLink, displayname=$displayname, profilePic=$profilePic, designation=$designation, email=$email)"
    }
}