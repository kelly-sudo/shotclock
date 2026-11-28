package pl.shotclock.budzik.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

/** Statystyki pobudek: czas wstania, seria dni i udane dni w tygodniu. */
class WakeStatsTest {
    // Stała strefa, żeby wynik nie zależał od komputera, na którym uruchamiamy testy.
    private val zone = ZoneId.of("Europe/Warsaw")

    /** Pobudka wyłączona danego dnia listopada 2026 o 7:00, po [seconds] sekundach dzwonienia. */
    private fun wake(day: Int, emergency: Boolean = false, seconds: Long = 60): WakeEvent {
        val dismissed = LocalDateTime.of(2026, 11, day, 7, 0).atZone(zone).toInstant().toEpochMilli()
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
    fun wakeUpTimeIsCountedFromRingStartToDismissal() {
        assertEquals(102, wakeUpSeconds(wake(day = 26, seconds = 102)))
    }

    @Test
    fun wakeUpTimeIsNeverNegative() {
        // Np. ktoś cofnął zegar w telefonie w trakcie dzwonienia.
        val event = wake(day = 26).let { it.copy(ringStartedAt = it.dismissedAt + 5_000) }

        assertEquals(0, wakeUpSeconds(event))
    }

    @Test
    fun streakCountsConsecutiveGoodDays() {
        val events = listOf(wake(24), wake(25), wake(26))

        assertEquals(3, currentStreak(events, zone))
    }

    @Test
    fun twoAlarmsOnTheSameDayCountAsOneDay() {
        val events = listOf(wake(25), wake(26), wake(26))

        assertEquals(2, currentStreak(events, zone))
    }

    @Test
    fun dayWithoutAnyAlarmDoesNotBreakTheStreak() {
        // Piątek 20 i poniedziałek 23: weekend bez budzika nie przerywa serii.
        val events = listOf(wake(20), wake(23))

        assertEquals(2, currentStreak(events, zone))
    }

    @Test
    fun emergencyDismissalBreaksTheStreak() {
        val events = listOf(wake(23), wake(24, emergency = true), wake(25), wake(26))

        assertEquals(2, currentStreak(events, zone))
    }

    @Test
    fun emergencyDismissalTodayMakesStreakZero() {
        val events = listOf(wake(25), wake(26), wake(26, emergency = true))

        assertEquals(0, currentStreak(events, zone))
    }

    @Test
    fun noHistoryMeansNoStreak() {
        assertEquals(0, currentStreak(emptyList(), zone))
    }

    @Test
    fun goodDaysInWeekStartOnMondayAndSkipEmergencies() {
        // 26 listopada 2026 to czwartek. Niedziela 22 należy do poprzedniego tygodnia.
        val events = listOf(wake(22), wake(23), wake(24, emergency = true), wake(26))

        val days = goodDaysInWeek(events, today = LocalDate.of(2026, 11, 26), zone = zone)

        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), days)
    }

    @Test
    fun summaryContainsAllValuesForSuccessScreen() {
        val today = wake(day = 26, seconds = 102)
        val events = listOf(wake(24), wake(25), today)

        val summary = summarizeWake(today, events, zone)

        assertEquals(102, summary.wakeUpSeconds)
        assertEquals(3, summary.streakDays)
        assertEquals(setOf(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY), summary.goodDaysThisWeek)
        assertEquals(DayOfWeek.THURSDAY, summary.today)
    }
}
