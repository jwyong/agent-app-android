package sg.com.agentapp.api.api_clients

/* Created by jayyong on 16/10/2018. */

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import sg.com.agentapp.api.RetroOkClient
import sg.com.agentapp.api.interfaces.S3Interface

object RetroS3Client {
    val BASE_URL = "https://agentapp-resources.s3.amazonaws.com/"

    private var retrofit: Retrofit
    private var s3Interface: S3Interface

    init {
        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(RetroOkClient().okHttpClient)
                .build()

        s3Interface = retrofit.create(S3Interface::class.java)

    }


    val s3API: S3Interface
        get() {
            return s3Interface
        }
}
