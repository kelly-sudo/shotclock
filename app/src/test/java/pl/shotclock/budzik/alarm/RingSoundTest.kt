package pl.shotclock.budzik.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.shotclock.budzik.data.Alarm

class RingSoundTest {
    private val easterEgg = "android.resource://pl.shotclock.budzik/123"
    private val chosen = "content://media/internal/audio/media/42"
    private val defaults = listOf("content://settings/system/alarm_alert")

    @Test
    fun onlyTwentyOneThirtySevenIsEasterEggTime() {
        assertTrue(isEasterEggTime(21, 37))
        assertFalse(isEasterEggTime(21, 38))
        assertFalse(isEasterEggTime(9, 37))
    }

    @Test
    fun easterEggSoundPlaysFirstAtTwentyOneThirtySeven() {
        val alarm = Alarm(hour = 21, minute = 37, soundUri = chosen)
        assertEquals(listOf(easterEgg, chosen) + defaults, ringSoundCandidates(alarm, easterEgg, defaults))
    }

    @Test
    fun withoutEasterEggFileChosenSoundPlays() {
        val alarm = Alarm(hour = 21, minute = 37, soundUri = chosen)
        assertEquals(listOf(chosen) + defaults, ringSoundCandidates(alarm, easterEggSound = null, defaults))
    }

    @Test
    fun otherTimesIgnoreEasterEgg() {
        val alarm = Alarm(hour = 7, minute = 30, soundUri = chosen)
        assertEquals(listOf(chosen) + defaults, ringSoundCandidates(alarm, easterEgg, defaults))
    }

    @Test
    fun defaultSoundAloneWhenNothingChosen() {
        val alarm = Alarm(hour = 7, minute = 30)
        assertEquals(defaults, ringSoundCandidates(alarm, easterEgg, defaults))
    }
}
