package pl.shotclock.budzik.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Onboarding prosi tylko o te zgody, które na danej wersji Androida naprawdę istnieją. */
class SetupStepsTest {

    @Test
    fun android8AsksOnlyForCameraAndBattery() {
        assertEquals(listOf(SetupStep.CAMERA, SetupStep.BATTERY), setupStepsFor(26))
    }

    @Test
    fun android12AsksForExactAlarms() {
        assertEquals(listOf(SetupStep.EXACT_ALARMS, SetupStep.CAMERA, SetupStep.BATTERY), setupStepsFor(31))
        assertEquals(listOf(SetupStep.EXACT_ALARMS, SetupStep.CAMERA, SetupStep.BATTERY), setupStepsFor(32))
    }

    @Test
    fun android13AsksForNotificationsButNotExactAlarms() {
        assertEquals(listOf(SetupStep.NOTIFICATIONS, SetupStep.CAMERA, SetupStep.BATTERY), setupStepsFor(33))
    }

    @Test
    fun android14AndNewerAlsoAskForFullScreen() {
        val expected = listOf(SetupStep.NOTIFICATIONS, SetupStep.FULL_SCREEN, SetupStep.CAMERA, SetupStep.BATTERY)
        assertEquals(expected, setupStepsFor(34))
        assertEquals(expected, setupStepsFor(37))
    }

    @Test
    fun samsungIsRecognizedRegardlessOfLetterCase() {
        assertTrue(isSamsung("samsung"))
        assertTrue(isSamsung("Samsung"))
        assertFalse(isSamsung("Google"))
    }
}
