package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.shotclock.budzik.data.AppSettings
import pl.shotclock.budzik.data.SettingsRepository
import pl.shotclock.budzik.ml.ClassifierMode
import pl.shotclock.budzik.ml.ModelInfo
import pl.shotclock.budzik.ml.classifierMode

/** Tyle razy trzeba dotknąć wersji aplikacji, żeby włączyć tryb deweloperski (jak w ustawieniach Androida). */
const val DeveloperModeTaps = 7

// Licznik dotknięć pokazujemy dopiero przy ostatnich kilku, żeby przypadkowe dotknięcie nic nie zdradzało.
private const val ShowCountdownFrom = 3
private const val StopTimeoutMillis = 5_000L

/**
 * Ile dotknięć zostało do trybu deweloperskiego po [taps] dotknięciach, albo null, gdy nie ma czego pokazać.
 * Czysta funkcja, sprawdzana w SettingsViewModelTest.
 */
fun developerTapsLeft(taps: Int): Int? {
    val left = DeveloperModeTaps - taps
    return if (left in 1..ShowCountdownFrom) left else null
}

/** Wszystko, co pokazuje ekran ustawień. */
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val classifierMode: ClassifierMode = ClassifierMode.Test,
    val appVersion: String = "",
    val modelVersion: String = "",
    // Ile dotknięć wersji zostało do trybu deweloperskiego (null: nie pokazujemy licznika).
    val developerTapsLeft: Int? = null,
)

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val modelInfo: ModelInfo,
    appVersion: String,
) : ViewModel() {

    private val versionTaps = MutableStateFlow(0)

    val uiState: StateFlow<SettingsUiState> =
        combine(repository.settings, versionTaps) { settings, taps ->
            SettingsUiState(
                settings = settings,
                classifierMode = classifierMode(settings.testMode, modelInfo.isAvailable),
                appVersion = appVersion,
                modelVersion = modelInfo.version,
                developerTapsLeft = if (settings.developerMode) null else developerTapsLeft(taps),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
            initialValue = SettingsUiState(appVersion = appVersion, modelVersion = modelInfo.version),
        )

    fun setDefaultSound(soundUri: String?) = save { repository.setDefaultSound(soundUri) }

    fun setDefaultVibration(vibration: Boolean) = save { repository.setDefaultVibration(vibration) }

    fun setTestMode(testMode: Boolean) = save { repository.setTestMode(testMode) }

    fun setConfidenceThreshold(threshold: Float) = save { repository.setConfidenceThreshold(threshold) }

    /** Dotknięcie wersji aplikacji. Siódme z rzędu włącza tryb deweloperski. */
    fun onVersionTapped() {
        if (uiState.value.settings.developerMode) return
        versionTaps.value += 1
        if (versionTaps.value >= DeveloperModeTaps) {
            versionTaps.value = 0
            save { repository.setDeveloperMode(true) }
        }
    }

    fun turnOffDeveloperMode() = save { repository.setDeveloperMode(false) }

    private fun save(action: suspend () -> Unit) {
        viewModelScope.launch { action() }
    }
}
