package net.baza.wapp.staticpackage

import android.os.Environment.getExternalStorageDirectory
import java.text.SimpleDateFormat
import java.util.*

class Constants
{
    companion object
    {
        const val CITIESBASENAME      = "cities"
        const val WEATHERDAYSBASENAME = "weatherdays"

        const val CITIESLASTUPDATESHAREDPREFS = "lastcitybaseupdate"
        const val LASTSELECTEDCITYSHAREDPREFS = "listselectedcity"

        const val SAVE_CITIES_ARCHIVE_MEMORY_PERMISSION = 11
        const val UPDATE_CITIES_SERVICES_NOTE_ID        = 17

        const val CITIESBASEURL = "https://bulk.openweathermap.org/sample/city.list.json.gz"
        const val WEATHERURL    = "https://api.openweathermap.org/data/2.5/onecall"
        const val ICONURL       = "https://openweathermap.org/img/w/"
        const val ICONEXT       = ".png"
        const val MYAPIKEY      = "35b8c173b1e5c3e6755b47db92b77242"

        fun getBaseUrl(url: String): String
        {
            return url.replace("[^\\\\/]+$".toRegex(), "")
        }

        private val DOWNLOADSDIR      = "${getExternalStorageDirectory().path}/Download/"
        val CITIESARCHIVENAME         = "${DOWNLOADSDIR}citiesarchive.zip"
        val CITIESJSONNAME            = "${DOWNLOADSDIR}citiesjson.json"

        const val UPDATEBASETIME = 7 * 24 * 3600 * 1000

        const val TEMP_DIFFERENT = 273.15

        val WEATHERDATEFORMAT = SimpleDateFormat("dd MM yyyy",          Locale("en", "EN"))
        val LASTUPDATEFORMAT  = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale("en", "EN"))
    }
}