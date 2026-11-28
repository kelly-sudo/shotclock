package pl.shotclock.budzik.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Jedna pobudka, czyli jeden wiersz tabeli `wake_events`: kiedy alarm zaczął dzwonić, kiedy go wyłączono i jak.
 * Czasy zapisujemy jako System.currentTimeMillis (liczba milisekund), bo SQLite nie ma typu „data i godzina”.
 */
@Entity(tableName = "wake_events")
data class WakeEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Alarm, który dzwonił. Celowo bez klucza obcego: po usunięciu alarmu jego historia zostaje.
    val alarmId: Long,
    val ringStartedAt: Long,
    val dismissedAt: Long,
    // Liczba zdjęć: nieudane ręczne zdjęcia plus to udane (przy wyłączeniu awaryjnym tylko nieudane).
    val attempts: Int,
    // Rozpoznany przedmiot i pewność modelu. Przy wyłączeniu awaryjnym oba są puste (null).
    val recognizedObject: RequiredObject?,
    val confidence: Float?,
    // true, gdy alarm wyłączono zadaniem matematycznym zamiast zdjęciem.
    val emergency: Boolean,
)
