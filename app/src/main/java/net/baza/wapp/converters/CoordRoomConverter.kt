package net.baza.wapp.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import net.baza.wapp.models.Coord

object CoordRoomConverter
{
    @TypeConverter
    @JvmStatic
    fun fromCoordToString(value: Coord): String
    {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToCoord(value: String): Coord
    {
        return Gson().fromJson(value, Coord::class.java)
    }
}