package pl.shotclock.budzik.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Statystyki ekranu historii: średni czas wstania, najlepsza seria i wykres z 7 dni. */
class HistoryStatsTest {
    // Stała strefa, żeby wynik nie zależał od komputera, na którym uruchamiamy testy.
    private val zone = ZoneId.of("Europe/Warsaw")
    private val today = LocalDate.of(2026, 11, 26)

    /** Pobudka wyłączona danego dnia listopada 2026 o [hour]:00, po [seconds] sekundach dzwonienia. */
    private fun wake(day: Int, seconds: Long = 60, emergency: Boolean = false, hour: Int = 7): WakeEvent {
        val dismissed = LocalDateTime.of(2026, 11, day, hour, 0).atZone(zone).toInstant().toEpochMilli()
        return WakeEvent(
            alarmId = 1,
            ringStartedAt = dismissed - seconds * 1000,
            dismissedAt = dismissed,
            attempts = 1,
            recognizedObject = if (emergency) null else RequiredObject.FIRST,
            confidence = if (emergency) null else 0.9f,
            emergency = emergency,
        )
    }

    @Test
    fun averageWakeUpTimeSkipsEmergencyDismissals() {
        // Awaryjne 600 s nie zawyża średniej z 60 s i 120 s.
        val events = listOf(wake(24, seconds = 60), wake(25, seconds = 120), wake(26, seconds = 600, emergency = true))

        assertEquals(90, averageWakeUpSeconds(events))
    }

    @Test
    fun noAverageWithoutPhotoDismissals() {
        assertNull(averageWakeUpSeconds(emptyList()))
        assertNull(averageWakeUpSeconds(listOf(wake(26, emergency = true))))
    }

    @Test
    fun bestStreakIsTheLongestRunInHistory() {
        // Seria 3 dni (10–12), przerwana awaryjnym 13, potem seria 2 dni (14–15).
        val events = listOf(
            wake(10), wake(11), wake(12),
            wake(13, emergency = true),
            wake(14), wake(15),
        )

        assertEquals(3, bestStreak(events, zone))
        assertEquals(2, currentStreak(events, zone))
    }

    @Test
    fun noHistoryMeansNoBestStreak() {
        assertEquals(0, bestStreak(emptyList(), zone))
    }

    @Test
    fun chartHasSevenDaysEndingToday() {
        val days = lastSevenDays(emptyList(), today, zone)

        assertEquals(7, days.size)
        assertEquals(LocalDate.of(2026, 11, 20), days.first().date)
        assertEquals(today, days.last().date)
    }

    @Test
    fun chartShowsEmptyDayAsNull() {
        val days = lastSevenDays(listOf(wake(26, seconds = 47)), today, zone)

        assertEquals(47, days.last().seconds)
        assertNull(days[5].seconds)
    }

    @Test
    fun chartAveragesTwoWakesOnTheSameDay() {
        val events = listOf(wake(25, seconds = 60, hour = 6), wake(25, seconds = 100, hour = 8))

        assertEquals(80, lastSevenDays(events, today, zone)[5].seconds)
    }

    @Test
    fun chartSkipsWakesOlderThanSevenDaysAndEmergencies() {
        val events = listOf(wake(19, seconds = 30), wake(26, emergency = true))

        val days = lastSevenDays(events, today, zone)

        assertEquals(List(7) { null }, days.map { it.seconds })
    }

    @Test
    fun historySummaryCombinesAllStats() {
        val events = listOf(wake(24, seconds = 60), wake(25, seconds = 120), wake(26, seconds = 90))

        val summary = summarizeHistory(events, today, zone)

        assertEquals(3, summary.streakDays)
        assertEquals(3, summary.bestStreakDays)
        assertEquals(90, summary.averageWakeUpSeconds)
        assertEquals(90, summary.lastSevenDays.last().seconds)
    }
}
