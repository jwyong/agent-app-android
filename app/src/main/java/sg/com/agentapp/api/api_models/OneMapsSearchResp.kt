package sg.com.agentapp.api.api_models

import com.google.gson.annotations.SerializedName

data class OneMapsSearchResp(

	@field:SerializedName("totalNumPages")
	val totalNumPages: Int? = null,

	@field:SerializedName("found")
	val found: Int? = null,

	@field:SerializedName("pageNum")
	val pageNum: Int? = null,

	@field:SerializedName("results")
	val results: List<OneMapsSearchResultsItem?>? = null
)