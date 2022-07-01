package net.baza.wapp.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import net.baza.wapp.API
import net.baza.wapp.R
import net.baza.wapp.db.AppDatabase
import net.baza.wapp.models.City
import net.baza.wapp.staticpackage.Constants
import net.baza.wapp.staticpackage.TextFunctions
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.zip.GZIPInputStream

class CitiesUpdateService : Service()
{
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notification = createNotification("Weather app updates cities base!")
                this.startForeground(Constants.UPDATE_CITIES_SERVICES_NOTE_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("StartServiceError", "Running Cities Update Service Error", e)
        }

        Retrofit.Builder().baseUrl(Constants.getBaseUrl(Constants.CITIESBASEURL)).build().create(
            API::class.java).downloadFile(
            Constants.CITIESBASEURL).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>)
            {
                val base = AppDatabase.invoke(this@CitiesUpdateService).cityBase()
                var count: Int
                val body = response.body()

                if (body != null && response.isSuccessful) {
                    val archive = File(Constants.CITIESARCHIVENAME)
                    Thread {
                        var inputStream: InputStream? = null
                        var outputStream: OutputStream? = null
                        try {
                            //writestreaminfile

                            inputStream = body.byteStream()
                            outputStream = FileOutputStream(archive)
                            val buffer = ByteArray(4096)
                            var read: Int
                            while (inputStream.read(buffer).also { read = it } != -1)
                                outputStream.write(buffer, 0, read)
                            outputStream.close()

                            createNotification("Unzipping Archive")

                            //unzip archive
                            val json = File(Constants.CITIESJSONNAME)
                            val zis = GZIPInputStream(BufferedInputStream(FileInputStream(archive)))

                            FileOutputStream(json).use {
                                    fout -> while (zis.read(buffer).also { count = it } != -1)
                                fout.write(buffer, 0, count)
                            }
                            zis.close()

                            val list = CopyOnWriteArrayList<City>()

                            createNotification("Parsing archive!")
                            list.addAll(
                                Gson().fromJson<CopyOnWriteArrayList<City>>(
                                    JsonReader(FileReader(json)), object : TypeToken<CopyOnWriteArrayList<City>>() {}.type))

                            createNotification("Updating base!")

                            if (list.size > 0) {
                                base.clear()
                                base.insertAll(list)
                                TextFunctions.saveText(Constants.CITIESLASTUPDATESHAREDPREFS, "${Date().time}", this@CitiesUpdateService)
                            }

                            var forRemove = archive
                            if (forRemove.exists())
                                forRemove.delete()
                            forRemove = json
                            if (forRemove.exists())
                                forRemove.delete()

                            removeNotification()
                        } catch (e: JSONException) {
                            Log.e("CitiesBaseParsingError", "JSON exception in cities parsing: ", e)
                        } catch (e: Exception) {
                            Log.e("CitiesBaseParsingError", "Exception in cities parsing: ", e)
                        } finally {
                            try {
                                inputStream?.close()
                                outputStream?.close()
                            } catch (e: IOException) {
                                Log.e("CitiesBaseParsingError", "Close streams error in cities parsing: ", e)
                            }
                        }
                    }.start()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                TextFunctions.showText(this@CitiesUpdateService, "Cities base update error!")
                Log.e("CitiesBaseParsingError", "Server not found!")
                removeNotification()
            }
        })

        return START_STICKY
    }

    private fun createNotification(text: String): Notification
    {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "${Constants.UPDATE_CITIES_SERVICES_NOTE_ID}")
            .setSmallIcon(R.drawable.cities_updates_icon)
            .setContentTitle("Please wait")
            .setContentText(text)
            .setProgress(0, 0, true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = this.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                "${Constants.UPDATE_CITIES_SERVICES_NOTE_ID}",
                "Update cities base notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(Constants.UPDATE_CITIES_SERVICES_NOTE_ID, builder.build())
        }

        return builder.build()
    }

    fun removeNotification() {
        try {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(Constants.UPDATE_CITIES_SERVICES_NOTE_ID)
        } catch (e: java.lang.Exception) {
            Log.e("RemoveNotificationError", "Remove Update Cities Service Notification Error: ", e)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("BindService", "ON BIND SERVICE! $intent")
        return null
    }

    /*override fun onRebind(intent: Intent)
    {
        println("ON REBIND SERVICE! $intent")
    }

    override fun onUnbind(intent: Intent): Boolean
    {
        println("ON UNBIND SERVICE: $intent")
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
    }*/
}
