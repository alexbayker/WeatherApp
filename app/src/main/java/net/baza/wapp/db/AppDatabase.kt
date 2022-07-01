package net.baza.wapp.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.baza.wapp.App
import net.baza.wapp.converters.CoordRoomConverter
import net.baza.wapp.converters.DateRoomConverter
import net.baza.wapp.converters.TempRoomConverter
import net.baza.wapp.converters.WeatherRoomConverter
import net.baza.wapp.models.City
import net.baza.wapp.models.WeatherDay
import net.baza.wapp.staticpackage.Constants
import net.baza.wapp.staticpackage.TextFunctions

@Database(entities = [City::class, WeatherDay::class], version = 7, exportSchema = false)
@TypeConverters(DateRoomConverter::class, CoordRoomConverter::class, TempRoomConverter::class, WeatherRoomConverter::class)
abstract class AppDatabase: RoomDatabase()
{
    abstract fun cityBase(): CityBase

    abstract fun weatherBase(): WeatherBase

    companion object {
        private val migration6_7 = object: Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                if (App.instance != null)
                    TextFunctions.saveText(Constants.CITIESLASTUPDATESHAREDPREFS, "0", App.instance!!)
            }
        }

        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "database.db"
        )
            .allowMainThreadQueries()
            .addMigrations(migration6_7)
            .build()
    }
}

