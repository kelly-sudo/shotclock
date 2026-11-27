package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.shotclock.budzik.alarm.RingController
import pl.shotclock.budzik.alarm.RingingAlarm
import pl.shotclock.budzik.data.RequiredObject

/** Wszystko, co pokazuje ekran dzwonienia. */
data class RingUiState(
    val hour: Int,
    val minute: Int,
    val label: String,
    val objectToFind: RequiredObject,
)

class RingViewModel(ringController: RingController) : ViewModel() {

    /** Stan dzwoniącego alarmu albo null, gdy alarm został wyłączony (wtedy ekran się zamyka). */
    val uiState: StateFlow<RingUiState?> =
        ringController.ringing
            .map { it?.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                // Od razu aktualny stan, żeby ekran nie zamknął się, zanim przeczyta dzwoniący alarm.
                initialValue = ringController.ringing.value?.toUiState(),
            )
}

private fun RingingAlarm.toUiState() = RingUiState(
    hour = alarm.hour,
    minute = alarm.minute,
    label = alarm.label,
    objectToFind = objectToFind,
)
