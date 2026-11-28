package pl.shotclock.budzik.alarm

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.shotclock.budzik.data.Alarm
import pl.shotclock.budzik.data.RequiredObject

/** Alarm, który właśnie dzwoni. */
data class RingingAlarm(
    val alarm: Alarm,
    // Konkretny przedmiot do sfotografowania. Przy „losowym” wylosowany w chwili rozpoczęcia dzwonienia.
    val objectToFind: RequiredObject,
    // Kiedy zaczął dzwonić (System.currentTimeMillis). Z tego liczymy czas wstania.
    val startedAtMillis: Long,
    // Nieudane ręczne zdjęcia. Po 10 pojawia się wyjście awaryjne (alarm/EmergencyExit.kt).
    val failedAttempts: Int = 0,
)

/**
 * Wspólny stan dzwonienia dla serwisu i ekranu dzwonienia.
 * Serwis wpisuje tu dzwoniący alarm, ekran go czyta, a wyłączenie alarmu zatrzymuje serwis.
 */
class RingController(private val context: Context) {
    private val _ringing = MutableStateFlow<RingingAlarm?>(null)

    /** Dzwoniący alarm albo null, gdy nic nie dzwoni. */
    val ringing: StateFlow<RingingAlarm?> = _ringing.asStateFlow()

    /** Wywołuje [RingService], gdy zaczyna dzwonić. */
    fun onRingingStarted(ringingAlarm: RingingAlarm) {
        _ringing.value = ringingAlarm
    }

    /** Wywołuje [RingService], gdy kończy pracę (także gdy zamknie go system). */
    fun onRingingStopped() {
        _ringing.value = null
    }

    /** Ręczne zdjęcie nie przekroczyło progu. Gdy nic nie dzwoni (podgląd z ustawień), nic się nie dzieje. */
    fun onPhotoRejected() {
        _ringing.update { it?.copy(failedAttempts = it.failedAttempts + 1) }
    }

    /** Wyłącza alarm: cisza i koniec wibracji. Wywołuje to RingViewModel po rozpoznaniu przedmiotu albo zadaniu matematycznym. */
    fun dismiss() {
        _ringing.value = null
        context.stopService(Intent(context, RingService::class.java))
    }
}
