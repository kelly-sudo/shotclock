package pl.shotclock.budzik.alarm

import org.junit.Assert.assertEquals
import org.junit.Test

class RingVolumeTest {
    private val delta = 0.0001f

    @Test
    fun withoutRisingVolumeIsAlwaysFull() {
        assertEquals(1f, ringVolume(elapsedMillis = 0, rising = false), delta)
    }

    @Test
    fun risingVolumeStartsQuiet() {
        assertEquals(START_VOLUME, ringVolume(elapsedMillis = 0, rising = true), delta)
    }

    @Test
    fun risingVolumeIsHalfwayInTheMiddle() {
        val expected = START_VOLUME + (1f - START_VOLUME) / 2
        assertEquals(expected, ringVolume(RISING_VOLUME_MILLIS / 2, rising = true), delta)
    }

    @Test
    fun risingVolumeStopsAtFull() {
        assertEquals(1f, ringVolume(RISING_VOLUME_MILLIS, rising = true), delta)
        assertEquals(1f, ringVolume(RISING_VOLUME_MILLIS * 10, rising = true), delta)
    }
}
