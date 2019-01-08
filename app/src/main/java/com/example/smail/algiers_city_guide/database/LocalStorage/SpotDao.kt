package com.example.smail.algiers_city_guide.database.LocalStorage

import android.arch.persistence.room.*
import com.example.smail.algiers_city_guide.entity.Favorite
import com.example.smail.algiers_city_guide.entity.Spot

@Dao
interface SpotDao {

    @Query("select * from spots ")
    fun getSpots(): List<Spot>

//    @Update
//    fun updateSpot(spot: Spot)

    @Query("select * from spots where id=:i")
    fun getSpot(i: Int):Spot

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSpot(vararg spot: Spot)

    @Delete
    fun deleteSpot(vararg spot: Spot)


    @Query("DELETE FROM spots")
    fun nukeTable()
}