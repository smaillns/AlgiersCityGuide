package smaillns.smail.dzair.database.LocalStorage

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import smaillns.smail.dzair.entity.Category
import smaillns.smail.dzair.entity.Favorite
import smaillns.smail.dzair.entity.Spot


@Database(entities = arrayOf(Spot::class, Category::class, Favorite::class), version = 2)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun getSpotDao() : SpotDao
    abstract fun getCategoryDao() : CategoryDao
    abstract fun getFavoriteDao() : FavoriteDao
}