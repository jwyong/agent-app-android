package sg.com.agentapp.api.interfaces

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface S3Interface {

    @GET()
    fun downloadMedia(@Url fileUrl: String): Call<ResponseBody>

    @GET()
    fun downloadProfileFromUrl(@Url fileUrl: String): Call<ResponseBody>
}