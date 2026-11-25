package pl.shotclock.budzik.data

import androidx.room.TypeConverter
import java.time.DayOfWeek

/**
 * SQLite nie ma typu „zbiór dni tygodnia”, więc zapisujemy go jako tekst z numerami dni,
 * np. "1,2,3,4,5" to dni robocze (1 = poniedziałek, 7 = niedziela). Pusty tekst to alarm jednorazowy.
 */
class Converters {
    @TypeConverter
    fun daysToText(days: Set<DayOfWeek>): String =
        days.sorted().joinToString(separator = ",") { it.value.toString() }

    @TypeConverter
    fun textToDays(text: String): Set<DayOfWeek> =
        if (text.isBlank()) {
            emptySet()
        } else {
            text.split(",").map { DayOfWeek.of(it.trim().toInt()) }.toSet()
        }
}
