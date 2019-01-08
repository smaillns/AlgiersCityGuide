package com.example.smail.algiers_city_guide.database.LocalStorage

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.example.smail.algiers_city_guide.entity.Category
import com.example.smail.algiers_city_guide.entity.Favorite
import com.example.smail.algiers_city_guide.entity.Spot


@Database(entities = arrayOf(Spot::class, Category::class, Favorite::class), version = 2)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun getSpotDao() : SpotDao
    abstract fun getCategoryDao() : CategoryDao
    abstract fun getFavoriteDao() : FavoriteDao
}