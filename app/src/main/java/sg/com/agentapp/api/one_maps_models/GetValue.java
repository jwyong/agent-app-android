package sg.com.agentapp.api.one_maps_models;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface GetValue {

//    @POST("v1/property/create")
//    @FormUrlEncoded
//    Call<String> postValue(@Field("propertyName") String propertyName);

    @GET("/privateapi/commonsvc/revgeocode?")
    Call<GetValueItemList> getValue12(@QueryMap Map<String, String> getValue);

    @GET("/commonapi/search?")
    Call<GetValueNameList> getValueName12(@QueryMap Map<String, String> getValueName);
}
