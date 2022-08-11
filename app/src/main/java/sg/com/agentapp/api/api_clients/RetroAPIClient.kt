package sg.com.agentapp.api.api_clients

/* Created by jayyong on 16/10/2018. */

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import sg.com.agentapp.api.RetroOkClient
import sg.com.agentapp.api.interfaces.ApiInterface

object RetroAPIClient {
    val BASE_URL = "http://52.221.88.24:8080/"
//    val BASE_URL = "http://192.168.0.133:8080/"

    private var retrofit: Retrofit
    private var apiInterface: ApiInterface

    init {
        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(RetroOkClient().okHttpClient)
                .build()

        apiInterface = retrofit.create(ApiInterface::class.java)

    }


    val api: ApiInterface
        get() {
            return apiInterface
        }
}
