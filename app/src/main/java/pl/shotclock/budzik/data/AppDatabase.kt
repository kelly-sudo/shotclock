package pl.shotclock.budzik.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Baza SQLite aplikacji. Na razie ma jedną tabelę z alarmami. Historia pobudek (WakeEvent) dojdzie w Etapie 5
 * i wtedy podniesiemy numer wersji.
 */
// Pliku ze schematem bazy nie eksportujemy, bo przyda się dopiero przy migracji do kolejnej wersji.
@Database(entities = [Alarm::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        private const val FILE_NAME = "shotclock.db"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, FILE_NAME).build()
    }
}
