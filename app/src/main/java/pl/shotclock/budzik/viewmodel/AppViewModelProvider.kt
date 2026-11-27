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
            EditAlarmViewModel(createSavedStateHandle(), shotClockApplication().container.alarmRepository)
        }
        initializer {
            RingViewModel(shotClockApplication().container.ringController)
        }
        initializer {
            val container = shotClockApplication().container
            CameraViewModel(container.ringController, container::createClassifier)
        }
    }
}

/** Obiekt aplikacji, w którym mieszka AppContainer. */
private fun CreationExtras.shotClockApplication(): ShotClockApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShotClockApplication
