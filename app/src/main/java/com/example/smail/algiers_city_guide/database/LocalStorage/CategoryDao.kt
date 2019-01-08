package com.example.smail.algiers_city_guide.database.LocalStorage

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.example.smail.algiers_city_guide.entity.Category
import com.example.smail.algiers_city_guide.entity.Spot

@Dao
interface CategoryDao {

    @Query("select * from categories ")
    fun getCategories(): List<Category>

    @Query("select * from categories where id=:i")
    fun getCategoryById(i:Long): Category

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCategory(vararg category: Category)

}