package com.example.smail.algiers_city_guide.entity

import android.arch.persistence.room.*
import android.support.annotation.ColorInt
import java.io.Serializable

@Entity(tableName = "categories")
data class Category(
               @PrimaryKey
               @ColumnInfo(name = "id")
               var id:Int = 1,
               @ColumnInfo(name = "name")
               var name:String = "",
               @ColumnInfo(name = "marker")
               var marker: String = "",
               @Ignore
               var spots:ArrayList<Spot>? = null):Serializable{



}