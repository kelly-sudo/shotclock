package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.shotclock.budzik.data.SettingsRepository

/** Pierwszy ekran po uruchomieniu aplikacji. */
enum class StartScreen {
    // Ustawienia jeszcze się czytają (to trwa ułamek sekundy). Widać wtedy samo tło.
    Loading,
    Onboarding,
    Alarms,
}

/** Decyduje, czy pokazać onboarding (tylko przy pierwszym uruchomieniu), i zapamiętuje, że już go przejrzano. */
class AppStartViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    // Czytamy ustawienia tylko raz, przy starcie. Późniejsze zmiany ustawień nie przestawiają już ekranu startowego.
    val startScreen: StateFlow<StartScreen> =
        flow {
            val done = settingsRepository.current().onboardingDone
            emit(if (done) StartScreen.Alarms else StartScreen.Onboarding)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, StartScreen.Loading)

    fun finishOnboarding() {
        viewModelScope.launch { settingsRepository.setOnboardingDone() }
    }
}
