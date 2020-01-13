package smaillns.smail.dzair.database.LocalStorage

import android.arch.persistence.room.*
import smaillns.smail.dzair.entity.Video

@Dao
interface VideoDao {
    @Query("select * from videos ")
    fun getVideos(): List<Video>


    @Query("select * from videos where id=:i")
    fun getVideo(i: Int): Video

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addVideo(vararg video: Video)

    @Delete
    fun deleteVideo(vararg video: Video)


    @Query("DELETE FROM videos")
    fun nukeTable()
}