package smaillns.smail.dzair.database.LocalStorage

import android.arch.persistence.room.*
import smaillns.smail.dzair.entity.Favorite

@Dao
interface FavoriteDao {

    @Query("select * from favorites ")
    fun getFavorites(): List<Favorite>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavorite(vararg favorite: Favorite)

    @Delete
    fun deleteFavorite(vararg favorite: Favorite)

    @Update
    fun updateFavorite(vararg favorite: Favorite)

   // @Query("DELETE FROM favorites")
   // fun nukeTable()
}