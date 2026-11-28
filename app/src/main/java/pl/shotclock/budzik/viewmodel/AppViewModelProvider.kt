package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pl.shotclock.budzik.ShotClockApplication

/**
 * Tworzy ViewModele i podaje im obiekty z AppContainer (ręczne wstrzykiwanie zależności, bez Hilta).
 * Ekran prosi o ViewModel tak: `viewModel(factory = AppViewModelProvider.Factory)`.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AlarmsViewModel(shotClockApplication().container.alarmRepository)
        }
        initializer {
            // SavedStateHandle zawiera argumenty z nawigacji, m.in. id edytowanego alarmu.
            val container = shotClockApplication().container
            EditAlarmViewModel(createSavedStateHandle(), container.alarmRepository, container.settingsRepository)
        }
        initializer {
            val container = shotClockApplication().container
            RingViewModel(container.ringController, container.wakeEventRepository)
        }
        initializer {
            EmergencyViewModel()
        }
        initializer {
            HistoryViewModel(shotClockApplication().container.wakeEventRepository.events)
        }
        initializer {
            val container = shotClockApplication().container
            ObjectsViewModel(container.alarmRepository.alarms, container.settingsRepository.settings, container.modelInfo)
        }
        initializer {
            val container = shotClockApplication().container
            SettingsViewModel(container.settingsRepository, container.modelInfo, container.appVersion)
        }
        initializer {
            val container = shotClockApplication().container
            CameraViewModel(container.ringController, container::createClassifier, container.settingsRepository.settings)
        }
    }
}

/** Obiekt aplikacji, w którym mieszka AppContainer. */
private fun CreationExtras.shotClockApplication(): ShotClockApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShotClockApplication
