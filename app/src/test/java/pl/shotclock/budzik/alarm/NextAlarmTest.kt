package pl.shotclock.budzik.alarm

import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.shotclock.budzik.data.Alarm

class NextAlarmTest {
    private val workdays = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

    /** 25 listopada 2026 to środa. */
    private fun wednesday(hour: Int, minute: Int, second: Int = 0) =
        LocalDateTime.of(2026, 11, 25, hour, minute, second)

    @Test
    fun testDateIsWednesday() {
        assertEquals(WEDNESDAY, wednesday(0, 0).dayOfWeek)
    }

    @Test
    fun oneTimeAlarmLaterTodayRingsToday() {
        val alarm = Alarm(hour = 7, minute = 30)

        assertEquals(wednesday(7, 30), nextRingTime(alarm, now = wednesday(6, 0)))
    }

    @Test
    fun oneTimeAlarmEarlierTodayRingsTomorrow() {
        val alarm = Alarm(hour = 7, minute = 30)

        assertEquals(wednesday(7, 30).plusDays(1), nextRingTime(alarm, now = wednesday(8, 0)))
    }

    @Test
    fun alarmSetForTheCurrentMinuteRingsTomorrow() {
        val alarm = Alarm(hour = 7, minute = 30)

        assertEquals(wednesday(7, 30).plusDays(1), nextRingTime(alarm, now = wednesday(7, 30)))
    }

    @Test
    fun workdayAlarmOnFridayEveningRingsOnMonday() {
        val alarm = Alarm(hour = 7, minute = 0, days = workdays)
        val fridayEvening = wednesday(20, 0).plusDays(2)

        val ringTime = nextRingTime(alarm, now = fridayEvening)

        assertEquals(MONDAY, ringTime.dayOfWeek)
        assertEquals(wednesday(7, 0).plusDays(5), ringTime)
    }

    @Test
    fun weeklyAlarmThatAlreadyRangTodayRingsNextWeek() {
        val alarm = Alarm(hour = 7, minute = 0, days = setOf(WEDNESDAY))

        assertEquals(wednesday(7, 0).plusDays(7), nextRingTime(alarm, now = wednesday(8, 0)))
    }

    @Test
    fun repeatingAlarmSkipsDaysThatAreNotSelected() {
        val alarm = Alarm(hour = 6, minute = 15, days = setOf(DayOfWeek.SATURDAY))

        assertEquals(wednesday(6, 15).plusDays(3), nextRingTime(alarm, now = wednesday(6, 0)))
    }

    @Test
    fun nextAlarmIsTheEarliestEnabledOne() {
        val later = Alarm(id = 1, hour = 9, minute = 0)
        val disabledEarlier = Alarm(id = 2, hour = 6, minute = 0, enabled = false)
        val earliest = Alarm(id = 3, hour = 7, minute = 30)

        val next = findNextAlarm(listOf(later, disabledEarlier, earliest), now = wednesday(5, 0))

        assertEquals(earliest, next?.alarm)
        assertEquals(wednesday(7, 30), next?.ringTime)
    }

    @Test
    fun alarmTomorrowMorningCanBeNextEvenIfItHasAnEarlierHour() {
        val tomorrowMorning = Alarm(id = 1, hour = 6, minute = 0)
        val tonight = Alarm(id = 2, hour = 23, minute = 0, days = setOf(FRIDAY))

        val next = findNextAlarm(listOf(tomorrowMorning, tonight), now = wednesday(22, 0))

        assertEquals(tomorrowMorning, next?.alarm)
    }

    @Test
    fun noNextAlarmWhenAllAreDisabled() {
        val alarms = listOf(Alarm(hour = 7, minute = 0, enabled = false))

        assertNull(findNextAlarm(alarms, now = wednesday(5, 0)))
    }

    @Test
    fun timeUntilRoundsMinutesUp() {
        val until = timeUntil(now = wednesday(0, 10, second = 30), ringTime = wednesday(7, 30))

        assertEquals(TimeUntil(days = 0, hours = 7, minutes = 20), until)
    }

    @Test
    fun timeUntilCountsWholeDays() {
        val until = timeUntil(now = wednesday(20, 0), ringTime = wednesday(7, 0).plusDays(5))

        assertEquals(TimeUntil(days = 4, hours = 11, minutes = 0), until)
    }
}
