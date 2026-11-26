package pl.shotclock.budzik.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Zapytania do tabeli `alarms`. Kod, który je wykonuje, Room generuje sam podczas kompilacji. */
@Dao
interface AlarmDao {
    /** Wszystkie alarmy od najwcześniejszej godziny. Flow wysyła nową listę po każdej zmianie w tabeli. */
    @Query("SELECT * FROM alarms ORDER BY hour, minute, id")
    fun observeAll(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms")
    suspend fun getAll(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getById(id: Long): Alarm?

    /** Zwraca id zapisanego alarmu. Alarm z id różnym od zera zostaje zapisany z tym samym id. */
    @Insert
    suspend fun insert(alarm: Alarm): Long

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
