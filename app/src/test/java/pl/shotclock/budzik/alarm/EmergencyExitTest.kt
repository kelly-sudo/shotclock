package pl.shotclock.budzik.alarm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Kiedy pojawia się „Nie mogę znaleźć przedmiotu”. */
class EmergencyExitTest {
    private val minute = 60_000L

    @Test
    fun hiddenAtTheStartOfRinging() {
        assertFalse(isEmergencyExitAvailable(failedAttempts = 0, ringingForMillis = 0))
    }

    @Test
    fun hiddenAfterNineFailedPhotos() {
        assertFalse(isEmergencyExitAvailable(failedAttempts = 9, ringingForMillis = minute))
    }

    @Test
    fun shownAfterTenFailedPhotos() {
        assertTrue(isEmergencyExitAvailable(failedAttempts = 10, ringingForMillis = minute))
    }

    @Test
    fun hiddenJustBeforeTenMinutes() {
        assertFalse(isEmergencyExitAvailable(failedAttempts = 0, ringingForMillis = 10 * minute - 1))
    }

    @Test
    fun shownAfterTenMinutesEvenWithoutPhotos() {
        assertTrue(isEmergencyExitAvailable(failedAttempts = 0, ringingForMillis = 10 * minute))
    }
}
