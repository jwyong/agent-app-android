package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName

data class OneMapsSearchResultsItem(

	@field:SerializedName("BUILDING")
	val bUILDING: String? = null,

	@field:SerializedName("LONGTITUDE")
	val lONGTITUDE: String? = null,

	@field:SerializedName("ADDRESS")
	val aDDRESS: String? = null,

	@field:SerializedName("LONGITUDE")
	val lONGITUDE: String? = null,

	@field:SerializedName("POSTAL")
	val pOSTAL: String? = null,

	@field:SerializedName("X")
	val X: String? = null,

	@field:SerializedName("Y")
	val Y: String? = null,

	@field:SerializedName("SEARCHVAL")
	val sEARCHVAL: String? = null,

	@field:SerializedName("ROAD_NAME")
	val rOADNAME: String? = null,

	@field:SerializedName("BLK_NO")
	val bLKNO: String? = null,

	@field:SerializedName("LATITUDE")
	val lATITUDE: String? = null
)