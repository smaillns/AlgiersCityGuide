package smaillns.smail.toulouse.database

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {

    val baseUrl = "https://6wq1blxp6i.execute-api.eu-west-3.amazonaws.com/dev/"
    val imageBaseUrl="https://toulouse-city-guide.s3.amazonaws.com"

    val endPoint: EndPoint by lazy {
        Retrofit.Builder().baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create()).build().create(EndPoint::class.java)
    }




}