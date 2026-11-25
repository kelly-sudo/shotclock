package pl.shotclock.budzik.data

import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun daysAreSavedAsSortedNumbers() {
        val workdays = setOf(FRIDAY, MONDAY, WEDNESDAY, TUESDAY, THURSDAY)

        assertEquals("1,2,3,4,5", converters.daysToText(workdays))
    }

    @Test
    fun savedTextIsReadBackAsTheSameDays() {
        val weekend = setOf(SATURDAY, SUNDAY)

        assertEquals(weekend, converters.textToDays(converters.daysToText(weekend)))
    }

    @Test
    fun oneTimeAlarmHasNoDays() {
        assertEquals("", converters.daysToText(emptySet()))
        assertTrue(converters.textToDays("").isEmpty())
    }
}
