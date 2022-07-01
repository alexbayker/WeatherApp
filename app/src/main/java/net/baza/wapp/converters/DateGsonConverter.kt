package net.baza.wapp.converters

import android.util.Log
import com.google.gson.JsonSerializer
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonPrimitive
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext
import net.baza.wapp.staticpackage.TextFunctions
import java.lang.Exception
import java.lang.reflect.Type
import java.util.*

object DateGsonConverter
{
    var dateserializer =
        JsonSerializer { src: Date, typeOfSrc: Type?, context: JsonSerializationContext? ->
            JsonPrimitive(src.time)
        }
    var datedeserializer =
        JsonDeserializer { json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext? ->
            try {
                if (json != null)
                {
                    val string = json.asString
                    if (TextFunctions.isLong(string)) {
                        if (string.length == 10)
                            return@JsonDeserializer Date(json.asLong * 1000)
                        else
                            return@JsonDeserializer Date(json.asLong)
                    }
                }
            } catch (e: Exception) {
                Log.e("ParseException", "Date deserialize from json error: ", e)
            }
            Date(0)
        }
}