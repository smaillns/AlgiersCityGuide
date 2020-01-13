package smaillns.smail.dzair.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "videos")
class Video(
        @PrimaryKey()
        @ColumnInfo(name = "id")
            var id: Int=1,
        @ColumnInfo(name = "title")
            var title: String = "",
        @ColumnInfo(name = "channelName")
            var channelName: String="",
        @ColumnInfo(name = "image")
            var image: String="",
        @ColumnInfo(name = "url")
            var url: String = ""): Serializable {
}