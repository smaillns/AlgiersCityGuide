package smaillns.smail.dzair.database

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {

    val baseUrl = "https://hq7l4rl1c5.execute-api.us-east-1.amazonaws.com/dev/"
    val imageBaseUrl="https://algiers-city-guide.s3.amazonaws.com"
//    val baseUrl = "https://algiers-city-guide.appspot.com"

    val endPoint: EndPoint by lazy {
        Retrofit.Builder().baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create()).build().create(EndPoint::class.java)
    }




}