package pl.shotclock.budzik.alarm

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.shotclock.budzik.data.Alarm
import pl.shotclock.budzik.data.RequiredObject

/** Alarm, który właśnie dzwoni. */
data class RingingAlarm(
    val alarm: Alarm,
    // Konkretny przedmiot do sfotografowania. Przy „losowym” wylosowany w chwili rozpoczęcia dzwonienia.
    val objectToFind: RequiredObject,
    // Kiedy zaczął dzwonić (System.currentTimeMillis). Przyda się do liczenia czasu wstania w Etapie 5.
    val startedAtMillis: Long,
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

    /** Wyłącza alarm: cisza, koniec wibracji, ekran dzwonienia się zamyka. W Etapie 5 wywoła to rozpoznanie przedmiotu. */
    fun dismiss() {
        _ringing.value = null
        context.stopService(Intent(context, RingService::class.java))
    }
}
