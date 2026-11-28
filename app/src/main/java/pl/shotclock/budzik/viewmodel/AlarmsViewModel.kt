package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.shotclock.budzik.alarm.TimeUntil
import pl.shotclock.budzik.alarm.findNextAlarm
import pl.shotclock.budzik.alarm.timeUntil
import pl.shotclock.budzik.data.Alarm
import pl.shotclock.budzik.data.AlarmRepository

private const val MILLIS_IN_MINUTE = 60_000L
// Po zamknięciu ekranu zbieranie danych trwa jeszcze 5 s, żeby obrót ekranu nie przeładowywał listy.
private const val STOP_TIMEOUT_MILLIS = 5_000L

/** Wszystko, co pokazuje ekran listy alarmów. */
data class AlarmsUiState(
    // Zanim baza odpowie, nie pokazujemy nic, żeby nie mignął pusty stan.
    val isLoading: Boolean = true,
    // Najbliższy włączony alarm, pokazywany w dużej karcie. null, gdy żaden alarm nie jest włączony.
    val nextAlarm: Alarm? = null,
    val timeUntilNext: TimeUntil? = null,
    // Pozostałe alarmy w kolejności godzin.
    val otherAlarms: List<Alarm> = emptyList(),
    // Ostatnio usunięty alarm. Dopóki nie jest null, ekran pokazuje pasek z przyciskiem „Cofnij”.
    val recentlyDeleted: Alarm? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && nextAlarm == null && otherAlarms.isEmpty()
}

class AlarmsViewModel(private val repository: AlarmRepository) : ViewModel() {

    private val recentlyDeleted = MutableStateFlow<Alarm?>(null)

    // Łączy listę alarmów z bazy, aktualny czas i ostatnio usunięty alarm.
    // Nowy stan powstaje po każdej zmianie w bazie, co minutę i po usunięciu alarmu.
    val uiState: StateFlow<AlarmsUiState> =
        combine(repository.alarms, currentMinute(), recentlyDeleted) { alarms, now, deleted ->
            val next = findNextAlarm(alarms, now)
            AlarmsUiState(
                isLoading = false,
                nextAlarm = next?.alarm,
                timeUntilNext = next?.let { timeUntil(now, it.ringTime) },
                otherAlarms = alarms.filter { it.id != next?.alarm?.id },
                recentlyDeleted = deleted,
            )
        }
            // Szukanie najbliższego alarmu odbywa się w tle, a nie na wątku, który rysuje ekran.
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = AlarmsUiState(),
            )

    fun setEnabled(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(alarm.id, enabled) }
    }

    /**
     * Usuwa alarm o podanym id i zapamiętuje go, żeby dało się go przywrócić.
     * Z tej samej funkcji korzysta przesunięcie karty na liście i przycisk kosza w edycji.
     */
    fun delete(alarmId: Long) {
        viewModelScope.launch {
            val alarm = repository.getAlarm(alarmId) ?: return@launch
            repository.delete(alarm)
            recentlyDeleted.value = alarm
        }
    }

    /** Przycisk „Cofnij”: przywraca ostatnio usunięty alarm. */
    fun undoDelete() {
        val alarm = recentlyDeleted.value ?: return
        recentlyDeleted.value = null
        viewModelScope.launch { repository.restore(alarm) }
    }

    /** Pasek z „Cofnij” zniknął, więc usunięcia nie da się już cofnąć. */
    fun forgetDeleted() {
        recentlyDeleted.value = null
    }
}

/** Aktualny czas, wysyłany od razu i potem na początku każdej minuty, żeby „Następny za…” się zmieniał. */
private fun currentMinute(): Flow<LocalDateTime> = flow {
    while (true) {
        emit(LocalDateTime.now())
        delay(MILLIS_IN_MINUTE - System.currentTimeMillis() % MILLIS_IN_MINUTE)
    }
}
