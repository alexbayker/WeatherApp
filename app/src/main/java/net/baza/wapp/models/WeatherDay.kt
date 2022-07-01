package net.baza.wapp.models

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.room.Entity
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import net.baza.wapp.API
import net.baza.wapp.R
import net.baza.wapp.converters.DateGsonConverter
import net.baza.wapp.db.AppDatabase
import net.baza.wapp.staticpackage.Constants
import net.baza.wapp.staticpackage.TextFunctions
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

@Entity(
    tableName = Constants.WEATHERDAYSBASENAME,
    primaryKeys = ["cityid", "date"])
class WeatherDay(
    @Expose var cityid: Long,

    @Bindable
    @SerializedName("dt")
    var date: Date,

    @Bindable
    @SerializedName("sunrise")
    var sunrise: Date,

    @Bindable
    @SerializedName("sunset")
    var sunset: Date,

    @Bindable
    @SerializedName("moonrise")
    var moonrise: Date,

    @Bindable
    @SerializedName("moonset")
    var moonset: Date,

    @Bindable
    @SerializedName("temp")
    var temp: Temp,

    @Bindable
    @SerializedName("weather")
    var weather: List<Weather>,

    @Bindable
    @Expose
    var update: Date = Date()
) : BaseObservable()
{

    @Bindable
    fun getDateString(): String
    {
        return if (this.date.time > 0)
                    Constants.WEATHERDATEFORMAT.format(this.date)
               else
                    ""
    }

    @Bindable
    fun getWeatherDesc(): String
    {
        return if (this.weather.isNotEmpty())
                   this.weather[0].desc
               else
                   ""
    }

    @Bindable
    fun getToday(): Boolean
    {
        return Constants.WEATHERDATEFORMAT.format(Date()).equals(this.getDateString())
    }

    @Bindable
    fun getIconUrl(): String
    {
        return if (this.weather.isNotEmpty())
                   "${Constants.ICONURL}${this.weather[0].icon}${Constants.ICONEXT}"
               else
                   ""
    }

    fun tempString(temp: Double): String
    {
        val int = temp.toInt()
        var answer = "$int °C"
        if (int > 0)
            answer = "+$answer"
        return answer
    }

    companion object {
        @JvmStatic
        @BindingAdapter("android:src", "progressbar")
        fun updateImage(view: ImageView, imageurl: String, loadingiconbar: ProgressBar) {
            Picasso.get().load(imageurl)
                .placeholder(R.color.colortransparent).error(R.color.colortransparent)
                .into(view, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        view.visibility = View.VISIBLE
                        loadingiconbar.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                        view.visibility = View.GONE
                        loadingiconbar.visibility = View.VISIBLE
                    }
                })
        }

        fun getWeather(context: Context, city: City, needtoast: Boolean = false, donecallback: (List<WeatherDay>) -> Unit = {}, errorcallback: () -> Unit = {})
        {
            Retrofit.Builder().baseUrl(Constants.getBaseUrl(city.getWeatherUrl())).build().create(API::class.java)
                .apiRequest(city.getWeatherUrl()).enqueue(object :
                    Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>)
                    {
                        val answer = response.body()!!.string()

                        try {
                            val json = JSONObject(answer)
                            if (json.has("daily"))
                            {
                                val builder = GsonBuilder()
                                    .registerTypeAdapter(Date::class.java, DateGsonConverter.dateserializer)
                                    .registerTypeAdapter(Date::class.java, DateGsonConverter.datedeserializer)

                                val list = builder.create().fromJson<CopyOnWriteArrayList<WeatherDay>>(
                                    json.getJSONArray("daily").toString(),
                                    object : TypeToken<CopyOnWriteArrayList<WeatherDay>>() {}.type)

                                val lastupdate = Date()

                                for (day in list)
                                {
                                    day.cityid = city.id
                                    day.update = lastupdate
                                }

                                if (list.isNotEmpty())
                                {
                                    AppDatabase.invoke(context).weatherBase().insertAll(list)
                                    donecallback(list)
                                }
                                else
                                    errorcallback()
                            }
                        } catch (e: Exception) {
                            val frombase = AppDatabase.invoke(context).weatherBase().getByCity(city.id)
                            if (frombase.isNotEmpty())
                                donecallback(frombase)
                            else
                            {
                                errorcallback()
                                if (needtoast)
                                    TextFunctions.showText(context, "Weather parsing error!")
                            }
                            Log.e("ParseError", "Weather parsing error: ", e)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        val frombase = AppDatabase.invoke(context).weatherBase().getByCity(city.id)
                        if (frombase.isNotEmpty())
                            donecallback(frombase)
                        else
                            errorcallback()
                        if ((t is SocketTimeoutException || t is UnknownHostException || t is ConnectException) && needtoast)
                        {
                            TextFunctions.showText(context, "Server not found!")
                        }
                        else {
                            if (needtoast)
                                TextFunctions.showText(context, "Loading error!")
                            Log.e("LoadingError", "Loading weather error: ", t)
                        }
                    }
                })
        }
    }
}