package net.baza.wapp.converters

import net.baza.wapp.models.Temp
import androidx.room.TypeConverter
import com.google.gson.Gson

object TempRoomConverter
{
    @TypeConverter
    @JvmStatic
    fun fromTempToString(value: Temp): String
    {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToTemp(value: String): Temp
    {
        return Gson().fromJson(value, Temp::class.java)
    }
}