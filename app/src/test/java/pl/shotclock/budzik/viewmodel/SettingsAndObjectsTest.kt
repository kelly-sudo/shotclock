package pl.shotclock.budzik.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.shotclock.budzik.data.Alarm
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.ml.ClassifierMode
import pl.shotclock.budzik.ml.classifierMode

/** Czyste funkcje ekranów ustawień i przedmiotów: tryb deweloperski, wybór modelu i liczba alarmów. */
class SettingsAndObjectsTest {

    @Test
    fun countdownAppearsOnlyForTheLastThreeTaps() {
        assertNull(developerTapsLeft(0))
        assertNull(developerTapsLeft(3))
        assertEquals(3, developerTapsLeft(4))
        assertEquals(1, developerTapsLeft(6))
        // Po siódmym dotknięciu tryb jest już włączony, więc licznik znika.
        assertNull(developerTapsLeft(DeveloperModeTaps))
    }

    @Test
    fun testModeAlwaysUsesFakeModel() {
        assertEquals(ClassifierMode.Test, classifierMode(testMode = true, modelAvailable = true))
        assertEquals(ClassifierMode.Test, classifierMode(testMode = true, modelAvailable = false))
    }

    @Test
    fun realModelNeedsModelFiles() {
        assertEquals(ClassifierMode.NoModel, classifierMode(testMode = false, modelAvailable = false))
        assertEquals(ClassifierMode.Real, classifierMode(testMode = false, modelAvailable = true))
    }

    @Test
    fun alarmsAreCountedPerObject() {
        val alarms = listOf(
            Alarm(hour = 7, minute = 0, requiredObject = RequiredObject.FIRST),
            Alarm(hour = 8, minute = 0, requiredObject = RequiredObject.FIRST),
            Alarm(hour = 9, minute = 0, requiredObject = RequiredObject.RANDOM),
        )

        val counts = countAlarmsByObject(alarms)

        assertEquals(2, counts[RequiredObject.FIRST])
        assertNull(counts[RequiredObject.SECOND])
        assertEquals(1, counts[RequiredObject.RANDOM])
    }
}
