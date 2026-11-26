package pl.shotclock.budzik.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

/** Jeden alarm, czyli jeden wiersz tabeli `alarms` w bazie SQLite. */
@Entity(tableName = "alarms")
data class Alarm(
    // Zero oznacza nowy alarm: Room sam nada mu kolejny numer przy zapisie.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    // Pusty zbiór oznacza alarm jednorazowy.
    val days: Set<DayOfWeek> = emptySet(),
    val enabled: Boolean = true,
    val label: String = "",
    val requiredObject: RequiredObject = RequiredObject.FIRST,
    // Adres dźwięku z systemowego wyboru dzwonków. null oznacza domyślny dźwięk alarmu w telefonie.
    val soundUri: String? = null,
    val vibration: Boolean = true,
    val risingVolume: Boolean = true,
)
