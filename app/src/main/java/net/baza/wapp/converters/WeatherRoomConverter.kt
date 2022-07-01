package net.baza.wapp.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.baza.wapp.models.Weather

object WeatherRoomConverter
{
    @TypeConverter
    @JvmStatic
    fun fromListWeatherToString(value: List<Weather>): String
    {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToListWeather(value: String): List<Weather>
    {
        return Gson().fromJson(value, object : TypeToken<List<Weather>>() {}.type)
    }
}