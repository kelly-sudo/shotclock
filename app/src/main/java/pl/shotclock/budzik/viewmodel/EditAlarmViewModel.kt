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
import pl.shotclock.budzik.data.SettingsRepository
import pl.shotclock.budzik.data.Weekdays

/** Wszystko, co pokazuje ekran edycji: alarm ze zmianami, których jeszcze nie zapisano. */
data class EditAlarmUiState(
    // Zanim alarm (albo ustawienia dla nowego alarmu) się wczyta, nie pokazujemy formularza, żeby nic nie przeskoczyło.
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    val alarm: Alarm = Alarm(hour = EditAlarmViewModel.DEFAULT_HOUR, minute = 0, days = Weekdays.workdays),
)

class EditAlarmViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: AlarmRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    // Id alarmu przychodzi z nawigacji (adres "edit_alarm?alarmId=3"). Brak id oznacza nowy alarm.
    private val alarmId: Long = savedStateHandle[ALARM_ID_ARG] ?: NEW_ALARM_ID

    private val _uiState = MutableStateFlow(EditAlarmUiState(isLoading = true))
    val uiState: StateFlow<EditAlarmUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val alarm = if (alarmId != NEW_ALARM_ID) repository.getAlarm(alarmId) else null
            // Jeśli alarmu nie ma w bazie (nowy albo już usunięty), tworzymy nowy z domyślnym dźwiękiem i wibracjami z ustawień.
            _uiState.value = if (alarm != null) {
                EditAlarmUiState(isNew = false, alarm = alarm)
            } else {
                val settings = settingsRepository.current()
                val defaults = EditAlarmUiState().alarm
                EditAlarmUiState(
                    alarm = defaults.copy(soundUri = settings.defaultSoundUri, vibration = settings.defaultVibration),
                )
            }
        }
    }

    fun setTime(hour: Int, minute: Int) = updateAlarm { it.copy(hour = hour, minute = minute) }

    fun setDays(days: Set<DayOfWeek>) = updateAlarm { it.copy(days = days) }

    fun setLabel(label: String) = updateAlarm { it.copy(label = label.take(LABEL_MAX_LENGTH)) }

    fun setRequiredObject(requiredObject: RequiredObject) = updateAlarm { it.copy(requiredObject = requiredObject) }

    fun setVibration(vibration: Boolean) = updateAlarm { it.copy(vibration = vibration) }

    fun setRisingVolume(risingVolume: Boolean) = updateAlarm { it.copy(risingVolume = risingVolume) }

    /** null oznacza domyślny dźwięk alarmu w telefonie. */
    fun setSound(soundUri: String?) = updateAlarm { it.copy(soundUri = soundUri) }

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
