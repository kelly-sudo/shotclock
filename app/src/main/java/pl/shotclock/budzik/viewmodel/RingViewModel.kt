package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.shotclock.budzik.alarm.RingController
import pl.shotclock.budzik.alarm.RingingAlarm
import pl.shotclock.budzik.alarm.isEmergencyExitAvailable
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.data.WakeEvent
import pl.shotclock.budzik.data.WakeEventRepository
import pl.shotclock.budzik.data.WakeSummary

// Co tyle sprawdzamy, czy minęło już 10 minut dzwonienia (wyjście awaryjne).
private const val ClockTickMillis = 5_000L

/** Wszystko, co pokazuje ekran dzwonienia. */
data class RingUiState(
    val hour: Int,
    val minute: Int,
    val label: String,
    val objectToFind: RequiredObject,
    // Czy pokazać „Nie mogę znaleźć przedmiotu” (po 10 nieudanych zdjęciach albo 10 minutach).
    val emergencyExitAvailable: Boolean = false,
)

/**
 * ViewModel całej pobudki w oknie RingActivity: dzwonienie, wyłączenie alarmu i zapis pobudki w historii.
 * Alarm wyłącza się tutaj, w jednym miejscu, niezależnie od tego, czy pomogło zdjęcie, czy zadanie matematyczne.
 */
class RingViewModel(
    private val ringController: RingController,
    private val wakeEventRepository: WakeEventRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    // Bieżący czas co kilka sekund, żeby wyjście awaryjne pojawiło się samo po 10 minutach dzwonienia.
    private val ticks = flow {
        while (true) {
            emit(clock())
            delay(ClockTickMillis)
        }
    }

    /** Stan dzwoniącego alarmu albo null, gdy alarm został wyłączony. */
    val uiState: StateFlow<RingUiState?> =
        combine(ringController.ringing, ticks) { ringing, now -> ringing?.toUiState(now) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                // Od razu aktualny stan, żeby ekran nie zamknął się, zanim przeczyta dzwoniący alarm.
                initialValue = ringController.ringing.value?.toUiState(clock()),
            )

    private val _summary = MutableStateFlow<WakeSummary?>(null)

    /** Podsumowanie do ekranu sukcesu. Pojawia się chwilę po wyłączeniu alarmu, gdy pobudka zapisze się w bazie. */
    val summary: StateFlow<WakeSummary?> = _summary.asStateFlow()

    /** Kamera rozpoznała przedmiot z pewnością [confidence]. */
    fun onObjectRecognized(confidence: Float) = finishWake(emergency = false, confidence = confidence)

    /** Zadanie matematyczne z wyjścia awaryjnego rozwiązane poprawnie. */
    fun onEmergencyTaskSolved() = finishWake(emergency = true, confidence = null)

    /** Wycisza alarm i zapisuje pobudkę. Drugie wywołanie nic nie robi, bo alarm już nie dzwoni. */
    private fun finishWake(emergency: Boolean, confidence: Float?) {
        val ringing = ringController.ringing.value ?: return
        ringController.dismiss()

        val event = WakeEvent(
            alarmId = ringing.alarm.id,
            ringStartedAt = ringing.startedAtMillis,
            dismissedAt = clock(),
            // Udane zdjęcie też jest próbą, więc doliczamy je do nieudanych.
            attempts = if (emergency) ringing.failedAttempts else ringing.failedAttempts + 1,
            recognizedObject = if (emergency) null else ringing.objectToFind,
            confidence = confidence,
            emergency = emergency,
        )
        viewModelScope.launch {
            _summary.value = wakeEventRepository.recordWake(event)
        }
    }
}

private fun RingingAlarm.toUiState(nowMillis: Long) = RingUiState(
    hour = alarm.hour,
    minute = alarm.minute,
    label = alarm.label,
    objectToFind = objectToFind,
    emergencyExitAvailable = isEmergencyExitAvailable(failedAttempts, nowMillis - startedAtMillis),
)
