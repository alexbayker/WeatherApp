package net.baza.wapp.db

import androidx.room.*
import net.baza.wapp.models.WeatherDay
import net.baza.wapp.staticpackage.Constants

@Dao
interface WeatherBase {

    @Query("SELECT * FROM ${Constants.WEATHERDAYSBASENAME} WHERE cityid = :cityid")
    fun getByCity(cityid: Long): List<WeatherDay>

    @Query("SELECT * FROM ${Constants.WEATHERDAYSBASENAME} WHERE date = :timestamp AND cityid = :cityid")
    fun getByDateAndCity(timestamp: Long, cityid: Long): WeatherDay

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(days: List<WeatherDay>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(day: WeatherDay)

    @Update
    fun update(day: WeatherDay)

    @Delete
    fun delete(day: WeatherDay)

    @Query("DELETE FROM ${Constants.WEATHERDAYSBASENAME}")
    fun clear()
}