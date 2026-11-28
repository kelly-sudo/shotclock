package pl.shotclock.budzik.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.shotclock.budzik.alarm.DefaultConfidenceThreshold

/** Ustawienia zapisują się w prawdziwym DataStore, tylko w pliku tymczasowym zamiast w telefonie. */
class SettingsRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    // DataStore pracuje w swoim zakresie korutyn. Zamykamy go po każdym teście, żeby zwolnić plik.
    private val scope = CoroutineScope(UnconfinedTestDispatcher() + SupervisorJob())
    private val repository by lazy {
        SettingsRepository(
            PreferenceDataStoreFactory.create(scope = scope) { File(folder.root, "test.preferences_pb") },
        )
    }

    @After
    fun closeDataStore() {
        scope.cancel()
    }

    @Test
    fun emptyFileGivesDefaults() = runTest {
        val settings = repository.current()

        assertNull(settings.defaultSoundUri)
        assertTrue(settings.defaultVibration)
        assertTrue(settings.testMode)
        assertFalse(settings.developerMode)
        assertEquals(DefaultConfidenceThreshold, settings.confidenceThreshold)
        assertEquals(0.55f, settings.confidenceThreshold)
    }

    @Test
    fun savedValuesAreReadBack() = runTest {
        repository.setDefaultSound("content://media/alarm/5")
        repository.setDefaultVibration(false)
        repository.setTestMode(false)
        repository.setDeveloperMode(true)
        repository.setConfidenceThreshold(0.7f)

        val settings = repository.current()

        assertEquals("content://media/alarm/5", settings.defaultSoundUri)
        assertFalse(settings.defaultVibration)
        assertFalse(settings.testMode)
        assertTrue(settings.developerMode)
        assertEquals(0.7f, settings.confidenceThreshold)
    }

    @Test
    fun onboardingIsShownUntilFinished() = runTest {
        assertFalse(repository.current().onboardingDone)

        repository.setOnboardingDone()

        assertTrue(repository.current().onboardingDone)
    }

    @Test
    fun defaultSoundCanBeResetToPhoneDefault() = runTest {
        repository.setDefaultSound("content://media/alarm/5")
        repository.setDefaultSound(null)

        assertNull(repository.current().defaultSoundUri)
    }

    @Test
    fun turningOffDeveloperModeRestoresDefaultThreshold() = runTest {
        repository.setDeveloperMode(true)
        repository.setConfidenceThreshold(0.85f)

        repository.setDeveloperMode(false)

        assertEquals(DefaultConfidenceThreshold, repository.current().confidenceThreshold)
    }

    @Test
    fun strangeThresholdIsKeptInSafeRange() = runTest {
        repository.setConfidenceThreshold(1.5f)
        assertEquals(0.95f, repository.current().confidenceThreshold)

        repository.setConfidenceThreshold(0f)
        assertEquals(0.3f, repository.current().confidenceThreshold)
    }

    @Test
    fun safeThresholdReplacesNotANumberWithDefault() {
        assertEquals(DefaultConfidenceThreshold, safeThreshold(Float.NaN))
    }

    @Test
    fun allThresholdOptionsAreInSafeRange() {
        ConfidenceThresholdOptions.forEach { assertEquals(it, safeThreshold(it)) }
        assertTrue(DefaultConfidenceThreshold in ConfidenceThresholdOptions)
    }
}
