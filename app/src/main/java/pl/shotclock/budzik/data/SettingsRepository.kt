package pl.shotclock.budzik.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Jedyne miejsce, przez które aplikacja czyta i zapisuje ustawienia.
 * DataStore podajemy z zewnątrz, żeby test (SettingsRepositoryTest) mógł użyć pliku tymczasowego.
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    /** Aktualne ustawienia. Wysyła nową wartość po każdej zmianie, więc ekrany odświeżają się same. */
    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        val defaults = AppSettings()
        AppSettings(
            defaultSoundUri = preferences[Keys.DEFAULT_SOUND],
            defaultVibration = preferences[Keys.DEFAULT_VIBRATION] ?: defaults.defaultVibration,
            testMode = preferences[Keys.TEST_MODE] ?: defaults.testMode,
            developerMode = preferences[Keys.DEVELOPER_MODE] ?: defaults.developerMode,
            confidenceThreshold = safeThreshold(preferences[Keys.THRESHOLD] ?: defaults.confidenceThreshold),
            previewObject = previewObjectOf(preferences[Keys.PREVIEW_OBJECT]) ?: defaults.previewObject,
            onboardingDone = preferences[Keys.ONBOARDING_DONE] ?: defaults.onboardingDone,
        )
    }

    /** Ustawienia w tej chwili, bez czekania na zmiany (np. przy tworzeniu nowego alarmu). */
    suspend fun current(): AppSettings = settings.first()

    /** null oznacza domyślny dźwięk alarmu w telefonie. */
    suspend fun setDefaultSound(soundUri: String?) {
        dataStore.edit { preferences ->
            if (soundUri == null) preferences.remove(Keys.DEFAULT_SOUND) else preferences[Keys.DEFAULT_SOUND] = soundUri
        }
    }

    suspend fun setDefaultVibration(vibration: Boolean) {
        dataStore.edit { it[Keys.DEFAULT_VIBRATION] = vibration }
    }

    suspend fun setTestMode(testMode: Boolean) {
        dataStore.edit { it[Keys.TEST_MODE] = testMode }
    }

    /** Wyłączenie trybu deweloperskiego przywraca też domyślny próg i przedmiot podglądu, żeby nic nie zostało zmienione „po cichu”. */
    suspend fun setDeveloperMode(developerMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.DEVELOPER_MODE] = developerMode
            if (!developerMode) {
                preferences.remove(Keys.THRESHOLD)
                preferences.remove(Keys.PREVIEW_OBJECT)
            }
        }
    }

    /** Przedmiot dla podglądu kamery. Losowy nie ma sensu w podglądzie, więc zapisujemy tylko konkretny przedmiot. */
    suspend fun setPreviewObject(previewObject: RequiredObject) {
        if (previewObject == RequiredObject.RANDOM) return
        dataStore.edit { it[Keys.PREVIEW_OBJECT] = previewObject.name }
    }

    suspend fun setConfidenceThreshold(threshold: Float) {
        dataStore.edit { it[Keys.THRESHOLD] = safeThreshold(threshold) }
    }

    /** Onboarding przejrzany: przy kolejnych uruchomieniach aplikacja startuje od razu na liście alarmów. */
    suspend fun setOnboardingDone() {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    // Nazwy kluczy w pliku ustawień. Nie zmieniamy ich, bo telefon straciłby zapisane wartości.
    private object Keys {
        val DEFAULT_SOUND = stringPreferencesKey("default_sound")
        val DEFAULT_VIBRATION = booleanPreferencesKey("default_vibration")
        val TEST_MODE = booleanPreferencesKey("test_mode")
        val DEVELOPER_MODE = booleanPreferencesKey("developer_mode")
        val THRESHOLD = floatPreferencesKey("confidence_threshold")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val PREVIEW_OBJECT = stringPreferencesKey("preview_object")
    }

    // Zapisujemy nazwę przedmiotu (FIRST / SECOND). Nieznana albo losowa nazwa daje null, czyli wartość domyślną.
    private fun previewObjectOf(name: String?): RequiredObject? =
        RequiredObject.entries.find { it.name == name && it != RequiredObject.RANDOM }

    companion object {
        /** Tworzy repozytorium z plikiem ustawień w pamięci aplikacji. Wywołujemy tylko raz, w AppContainer. */
        fun create(context: Context): SettingsRepository =
            SettingsRepository(
                PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") },
            )
    }
}
