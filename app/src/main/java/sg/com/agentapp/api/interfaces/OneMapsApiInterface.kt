package sg.com.agentapp.api.interfaces

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import sg.com.agentapp.api.api_models.OneMapsSearchResp
import sg.com.agentapp.api.one_maps_models.GetValueItemList


/**
 * Created by jay on 16/10/18.
 */

interface OneMapsApiInterface {
    @GET("/commonapi/search?")
    fun search(@Query("searchVal") searchVal: String, @Query("returnGeom") returnGeom: String? = "Y",
               @Query("getAddrDetails") getAddrDetails: String? = "Y", @Query("pageNum") pageNum: String? = null )
            : Call<OneMapsSearchResp>
//
//    @GET("/privateapi/commonsvc/revgeocode?")
//    fun getValue12(@QueryMap getValue: Map<String, String>): Call<GetValueItemList>
}
