package net.baza.wapp.models

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import net.baza.wapp.services.CitiesUpdateService
import net.baza.wapp.staticpackage.Constants
import net.baza.wapp.staticpackage.TextFunctions
import java.io.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

@Entity(tableName = Constants.CITIESBASENAME)
class City(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("coord")
    val coord: Coord
) {
    fun getWeatherUrl(): String
    {
        return "${Constants.WEATHERURL}?lat=${this.coord.lat}&lon=${this.coord.lon}&exclude=hourly,minutely&appid=${Constants.MYAPIKEY}"
    }

    override fun toString(): String {
        return this.name
    }
    companion object
    {
        fun defaultCities(context: Context): List<City>
        {
            val list = CopyOnWriteArrayList<City>()
            try
            {
                val reader = BufferedReader(InputStreamReader(context.assets.open("hardcodecities.json")))
                list.addAll(Gson().fromJson<CopyOnWriteArrayList<City>>(reader, object : TypeToken<CopyOnWriteArrayList<City>>() {}.type))
            }
            catch (e: Exception)
            {
                Log.e("ParseError", "Hardcode cities parse error: ", e)
            }
            return list
        }

        private fun checkValidBase(context: Context): Boolean
        {
            val timestamp = TextFunctions.loadText(Constants.CITIESLASTUPDATESHAREDPREFS, context)
            if (TextFunctions.isLong(timestamp))
                return Date().time - timestamp.toLong() < Constants.UPDATEBASETIME
            return false
        }

        fun updateCities(context: Context, loadinglayout: View? = null)
        {
            if (!checkValidBase(context))
            {
                if (loadinglayout != null && context is Activity)
                {
                    context.runOnUiThread { loadinglayout.visibility = View.VISIBLE }
                }

                val intent = Intent(context, CitiesUpdateService::class.java)
                intent.action = "${Constants.UPDATE_CITIES_SERVICES_NOTE_ID}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(intent)
                else
                    context.startService(intent)
            }
        }
    }
}
