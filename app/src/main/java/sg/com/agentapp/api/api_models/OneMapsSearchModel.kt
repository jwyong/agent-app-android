package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName

data class OneMapsSearchModel(
        // body
        @field:SerializedName("searchVal")
        val searchVal: String,

        @field:SerializedName("returnGeom")
        val returnGeometry: String? = "Y",

        @field:SerializedName("getAddrDetails")
        val getAddrDetails: String? = "Y",

        @field:SerializedName("pageNum")
        val pageNumber: String? = null

) {
}