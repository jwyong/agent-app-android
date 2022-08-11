package sg.com.agentapp.api.interfaces

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import sg.com.agentapp.api.api_models.*


/**
 * Created by jay on 16/10/18.
 */

interface ApiInterface {
    //===== registration
    @POST("api/v1.0/otp/register_fcm")
    fun register(@Body model: RegisterFcmModel): Call<RegResModel>

    @POST("api/v1.0/otp/check_phone")
    fun checkPhone(@Body model: ApiCheckPhone): Call<ApiCheckPhone>

    @Multipart
    @POST("api/v1.0/user/profile/update")
    fun updateUserProfile(@Header("Authorization") header: String,
                          @Part file: MultipartBody.Part,
                          @Part gotFile: MultipartBody.Part,
                          @Part displayName: MultipartBody.Part,
                          @Part email: MultipartBody.Part,
                          @Part website: MultipartBody.Part,
                          @Part designation: MultipartBody.Part
    ): Call<BasicResponseModel>

    @GET("api/v1.0/user/profile/get")
    fun getUserProfile(): Call<GetUserProfilfeResp>

    //===== FCM
    @POST("api/v1.0/notification/register_token")
    fun sendFcmToken(@Body model: SendFcmToken, @Header("Authorization") access: String): Call<BasicResponseModel>


    //===== find agent
    @POST("api/v1.0/agent/search_agent")
    fun getAgent(@Body jsonObject: JsonObject): Call<List<FindAgentModel>>

    @POST("api/v1.0/agent/get_agent")
    fun getAgentDeatils(@Body jsonObject: JsonObject, @Header("Authorization") header: String): Call<FindAgentModel>


    //===== media: image
    @Multipart
    @POST("api/v1.0/files/upload")
    fun uploadMedia(@Header("Authorization") header: String,
                    @Part media_type: MultipartBody.Part,
                    @Part message_id: MultipartBody.Part,
                    @Part sender_jid: MultipartBody.Part,
                    @Part receiver_jid: MultipartBody.Part,
                    @Part resources: MultipartBody.Part,
                    @Part subject: MultipartBody.Part,
                    @Part body: MultipartBody.Part,
                    @Part image_thumbnail: MultipartBody.Part,
                    @Part file_name: MultipartBody.Part,
                    @Part file: MultipartBody.Part): Call<JsonObject>

    @POST("api/v1.0/files/get_files")
    fun getMediaURL(@Header("Authorization") access: String,
                    @Body jsonObject: JsonObject): Call<JsonObject>


    /**
     * Appointment
     */

    @POST("api/v1.0/appointment/agent/create")
    suspend fun createAppointment(@Header("Authorization") access: String,
                                  @Body body: CreateApptReq): Response<CreateApptRes>

    @POST("api/v1.0/appointment/agent/update")
    suspend fun updateAppointment(@Header("Authorization") access: String,
                                  @Body body: CreateApptReq): Response<CreateApptRes>

    @POST("api/v1.0/appointment/agent/accept")
    suspend fun acceptAppointment(@Header("Authorization") access: String,
                                  @Body body: CreateApptReq): Response<CreateApptRes>

    @POST("api/v1.0/appointment/agent/reject")
    suspend fun rejectAppointment(@Header("Authorization") access: String,
                                  @Body body: CreateApptReq): Response<CreateApptRes>

    @POST("api/v1.0/appointment/agent/reject")
    suspend fun delAppointment(@Header("Authorization") access: String,
                               @Body body: CreateApptReq): Response<CreateApptRes>

    @POST("api/v1.0/appointment/agent/get")
    suspend fun getAppointment(@Header("Authorization") access: String,
                               @Body body: CreateApptRes): Response<CreateApptReq>
}
