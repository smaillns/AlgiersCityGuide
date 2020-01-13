package smaillns.smail.dzair.database.LocalStorage

import android.arch.persistence.room.Room
import android.content.Context

object RoomService {
    lateinit var context : Context

    val appDataBase by lazy {
        Room.databaseBuilder(context, AppDataBase::class.java, "db6").allowMainThreadQueries().build()
    }

}