package smaillns.smail.toulouse.database.LocalStorage

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import smaillns.smail.toulouse.entity.Category
import smaillns.smail.toulouse.entity.Favorite
import smaillns.smail.toulouse.entity.Spot
import smaillns.smail.toulouse.entity.Video


@Database(entities = arrayOf(Spot::class, Category::class, Favorite::class, Video::class), version = 2)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun getSpotDao() : SpotDao
    abstract fun getCategoryDao() : CategoryDao
    abstract fun getFavoriteDao() : FavoriteDao
    abstract fun getVideoDao(): VideoDao
}