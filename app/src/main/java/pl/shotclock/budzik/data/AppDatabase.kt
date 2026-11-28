package pl.shotclock.budzik.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Baza SQLite aplikacji: alarmy (`alarms`) i historia pobudek (`wake_events`).
 * Wersja 2 dodała historię. Każda zmiana tabel to nowa wersja i migracja, żeby nie skasować zapisanych alarmów.
 */
// Pliku ze schematem bazy nie eksportujemy. Migracje piszemy ręcznie (patrz MIGRATION_1_2).
@Database(entities = [Alarm::class, WakeEvent::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun wakeEventDao(): WakeEventDao

    companion object {
        private const val FILE_NAME = "shotclock.db"

        /**
         * Z wersji 1 (same alarmy) do 2: dochodzi pusta tabela z historią pobudek, alarmy zostają bez zmian.
         * Kolumny muszą dokładnie pasować do klasy WakeEvent, inaczej Room zgłosi błąd przy otwarciu bazy.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `wake_events` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `alarmId` INTEGER NOT NULL,
                        `ringStartedAt` INTEGER NOT NULL,
                        `dismissedAt` INTEGER NOT NULL,
                        `attempts` INTEGER NOT NULL,
                        `recognizedObject` TEXT,
                        `confidence` REAL,
                        `emergency` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, FILE_NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
