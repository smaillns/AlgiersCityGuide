package com.example.smail.algiers_city_guide.database

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {

    val baseUrl = "http://192.168.43.28:8088"

    val endPoint:EndPoint by lazy {
        Retrofit.Builder().baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create()).build().create(EndPoint::class.java)
    }


}