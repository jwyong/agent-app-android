package sg.com.agentapp.api.api_clients

/* Created by jayyong on 16/10/2018. */

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import sg.com.agentapp.api.RetroOkClient
import sg.com.agentapp.api.interfaces.OneMapsApiInterface

object RetroOneMapAPIClient {
    val BASE_URL = "https://developers.onemap.sg/"

    private var retrofit: Retrofit
    private var apiInterface: OneMapsApiInterface

    init {
        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(RetroOkClient().okHttpClient)
                .build()

        apiInterface = retrofit.create(OneMapsApiInterface::class.java)

    }


    val api: OneMapsApiInterface
        get() {
            return apiInterface
        }
}
