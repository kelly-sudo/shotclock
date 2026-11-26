package pl.shotclock.budzik.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.shotclock.budzik.ShotClockApplication

/**
 * Ustawia wszystkie włączone alarmy jeszcze raz, gdy system je zapomniał albo ich termin się przesunął:
 * po restarcie telefonu, po aktualizacji aplikacji, po zmianie godziny lub strefy czasowej
 * i po udzieleniu zgody na dokładne alarmy.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in handledActions) return

        val repository = (context.applicationContext as ShotClockApplication).container.alarmRepository
        // goAsync pozwala dokończyć odczyt z bazy po wyjściu z onReceive.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val handledActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
    }
}
