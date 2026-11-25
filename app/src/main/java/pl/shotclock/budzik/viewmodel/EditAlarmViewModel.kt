package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.shotclock.budzik.data.Alarm
import pl.shotclock.budzik.data.AlarmRepository
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.data.Weekdays

/** Wszystko, co pokazuje ekran edycji: alarm ze zmianami, których jeszcze nie zapisano. */
data class EditAlarmUiState(
    // Zanim alarm wczyta się z bazy, nie pokazujemy formularza, żeby godzina nie przeskoczyła.
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    val alarm: Alarm = Alarm(hour = EditAlarmViewModel.DEFAULT_HOUR, minute = 0, days = Weekdays.workdays),
)

class EditAlarmViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: AlarmRepository,
) : ViewModel() {

    // Id alarmu przychodzi z nawigacji (adres "edit_alarm?alarmId=3"). Brak id oznacza nowy alarm.
    private val alarmId: Long = savedStateHandle[ALARM_ID_ARG] ?: NEW_ALARM_ID

    private val _uiState = MutableStateFlow(EditAlarmUiState(isLoading = alarmId != NEW_ALARM_ID))
    val uiState: StateFlow<EditAlarmUiState> = _uiState.asStateFlow()

    init {
        if (alarmId != NEW_ALARM_ID) {
            viewModelScope.launch {
                val alarm = repository.getAlarm(alarmId)
                // Jeśli alarmu już nie ma w bazie, ekran po prostu utworzy nowy.
                _uiState.value = if (alarm != null) {
                    EditAlarmUiState(isNew = false, alarm = alarm)
                } else {
                    EditAlarmUiState()
                }
            }
        }
    }

    fun setTime(hour: Int, minute: Int) = updateAlarm { it.copy(hour = hour, minute = minute) }

    fun setDays(days: Set<DayOfWeek>) = updateAlarm { it.copy(days = days) }

    fun setLabel(label: String) = updateAlarm { it.copy(label = label.take(LABEL_MAX_LENGTH)) }

    fun setRequiredObject(requiredObject: RequiredObject) = updateAlarm { it.copy(requiredObject = requiredObject) }

    fun setVibration(vibration: Boolean) = updateAlarm { it.copy(vibration = vibration) }

    fun setRisingVolume(risingVolume: Boolean) = updateAlarm { it.copy(risingVolume = risingVolume) }

    /** Zapisuje alarm w bazie i włącza go. [onSaved] wywołujemy dopiero po zapisie, żeby lista od razu go pokazała. */
    fun save(onSaved: () -> Unit) {
        val alarm = _uiState.value.alarm.copy(label = _uiState.value.alarm.label.trim(), enabled = true)
        viewModelScope.launch {
            repository.save(alarm)
            onSaved()
        }
    }

    private fun updateAlarm(change: (Alarm) -> Alarm) {
        _uiState.update { state -> state.copy(alarm = change(state.alarm)) }
    }

    companion object {
        /** Nazwa argumentu z id alarmu w adresie ekranu edycji. */
        const val ALARM_ID_ARG = "alarmId"
        const val NEW_ALARM_ID = 0L
        const val DEFAULT_HOUR = 7

        // Dłuższa etykieta nie zmieściłaby się na karcie alarmu obok godziny i ilustracji.
        const val LABEL_MAX_LENGTH = 30
    }
}
