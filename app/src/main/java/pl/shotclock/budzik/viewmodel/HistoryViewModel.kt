package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.shotclock.budzik.data.HistorySummary
import pl.shotclock.budzik.data.WakeEvent
import pl.shotclock.budzik.data.summarizeHistory

// Tyle ostatnich pobudek pokazuje lista pod wykresem.
private const val RecentEventsCount = 10
private const val StopTimeoutMillis = 5_000L

/** Wszystko, co pokazuje ekran historii. */
data class HistoryUiState(
    // Zanim baza odpowie, nie pokazujemy nic, żeby nie mignął pusty stan.
    val isLoading: Boolean = true,
    val summary: HistorySummary? = null,
    // Ostatnie pobudki, od najnowszej.
    val recentEvents: List<WakeEvent> = emptyList(),
    val today: LocalDate = LocalDate.now(),
    val zone: ZoneId = ZoneId.systemDefault(),
) {
    val isEmpty: Boolean get() = !isLoading && recentEvents.isEmpty()
}

/**
 * Historia pobudek: czyta zapisane pobudki z bazy i liczy z nich statystyki czystą funkcją [summarizeHistory].
 * [today] i [zone] podajemy w testach, żeby wynik nie zależał od daty i strefy komputera.
 */
class HistoryViewModel(
    events: Flow<List<WakeEvent>>,
    private val today: () -> LocalDate = LocalDate::now,
    private val zone: () -> ZoneId = ZoneId::systemDefault,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> =
        events.map { all ->
            val now = today()
            HistoryUiState(
                isLoading = false,
                summary = summarizeHistory(all, now, zone()),
                recentEvents = all.take(RecentEventsCount),
                today = now,
                zone = zone(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
            initialValue = HistoryUiState(),
        )
}
