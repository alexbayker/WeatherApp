package net.baza.wapp.db

import androidx.room.*
import net.baza.wapp.staticpackage.Constants
import net.baza.wapp.models.City

@Dao
interface CityBase
{
    @Query("SELECT * FROM ${Constants.CITIESBASENAME}")
    fun getAll(): List<City>

    @Query("SELECT * FROM ${Constants.CITIESBASENAME} WHERE id = :id")
    fun getById(id: Long): List<City>

    @Query("SELECT * FROM ${Constants.CITIESBASENAME} WHERE name LIKE '%' || :request || '%'")
    fun containsSearch(request: String): List<City>

    @Query("SELECT COUNT(id) FROM ${Constants.CITIESBASENAME}")
    fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(cities: List<City>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(city: City)

    @Update
    fun update(city: City)

    @Delete
    fun delete(city: City)

    @Query("DELETE FROM ${Constants.CITIESBASENAME}")
    fun clear()
}