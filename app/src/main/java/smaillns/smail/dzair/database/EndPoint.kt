package smaillns.smail.dzair.database


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import smaillns.smail.dzair.entity.*
import java.util.*

interface EndPoint {

    @GET("spots")
    fun getSpots():Call<ArrayList<Spot>>

    @GET("categories")
    fun getCategories():Call<ArrayList<Category>>

    @GET("categories/{id}")
    fun getCategories(@Path("id") id:Int):Call<ArrayList<Spot>>

    @GET("nbr")
    fun getNbrSpot():Call<List<TestDB>>

    @GET("images/{id}")
    fun getImages(@Path("id") id:Int):Call<ArrayList<Image_>>

    @GET("nbrimages/{id}")
    fun getNbrImages(@Path("id") id:Int):Call<List<TestDB>>

    @GET("videos")
    fun getVideos():Call<ArrayList<Video>>

    @GET("nbvideos")
    fun getNbrVideos():Call<List<TestDB>>
}