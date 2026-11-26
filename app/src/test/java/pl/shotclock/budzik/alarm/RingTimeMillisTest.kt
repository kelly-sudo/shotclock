package pl.shotclock.budzik.alarm

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class RingTimeMillisTest {
    private val warsaw = ZoneId.of("Europe/Warsaw")

    @Test
    fun normalDayKeepsTheSameHour() {
        val ringTime = LocalDateTime.of(2026, 11, 26, 7, 30)
        val expected = ZonedDateTime.of(ringTime, warsaw).toInstant().toEpochMilli()
        assertEquals(expected, ringTimeMillis(ringTime, warsaw))
    }

    @Test
    fun hourSkippedBySpringTimeChangeRingsOneHourLater() {
        // 29 marca 2026 w Polsce zegary przeskakują z 2:00 na 3:00, więc 2:30 nie istnieje.
        val ringTime = LocalDateTime.of(2026, 3, 29, 2, 30)
        val expected = ZonedDateTime.of(LocalDateTime.of(2026, 3, 29, 3, 30), warsaw).toInstant().toEpochMilli()
        assertEquals(expected, ringTimeMillis(ringTime, warsaw))
    }

    @Test
    fun sameHourInAnotherTimeZoneIsADifferentMoment() {
        val ringTime = LocalDateTime.of(2026, 11, 26, 7, 30)
        val london = ZoneId.of("Europe/London")
        // Londyn jest zimą godzinę za Warszawą, więc 7:30 w Londynie to godzinę później.
        assertEquals(60 * 60 * 1000L, ringTimeMillis(ringTime, london) - ringTimeMillis(ringTime, warsaw))
    }
}
