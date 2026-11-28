package pl.shotclock.budzik.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/** Zapytania do tabeli `wake_events` z historią pobudek. */
@Dao
interface WakeEventDao {
    /** Zwraca id zapisanej pobudki. */
    @Insert
    suspend fun insert(event: WakeEvent): Long

    /** Wszystkie pobudki, od najnowszej. */
    @Query("SELECT * FROM wake_events ORDER BY dismissedAt DESC")
    suspend fun getAll(): List<WakeEvent>
}
