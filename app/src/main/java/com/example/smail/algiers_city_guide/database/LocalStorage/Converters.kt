package com.example.smail.algiers_city_guide.database.LocalStorage
import android.arch.persistence.room.TypeConverter
import com.example.smail.algiers_city_guide.entity.Spot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
//    @TypeConverter
//    fun fromString(value: String): ArrayList<Spot> {
//        val listType = object : TypeToken<ArrayList<Spot>>() {
//
//        }.type
//        return Gson().fromJson<Any>(value, listType) as ArrayList<Spot>
//    }
//
//    @TypeConverter
//    fun fromArrayList(list: ArrayList<Spot>): String {
//        val gson = Gson()
//        return gson.toJson(list)
//    }

    @TypeConverter
    fun fromString(value: String): ArrayList<String>? {
        val listType = object : TypeToken<ArrayList<String>>() {

        }.type
        return Gson().fromJson<Any>(value, listType) as ArrayList<String>?
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }



}