package smaillns.smail.dzair.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "favorites")
data class Favorite (
        @PrimaryKey()
        @ColumnInfo(name = "id")
        var id : Int = 1,
        @ColumnInfo(name = "fav")
        var fav:Int = 1){

}