package pl.shotclock.budzik.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId
import pl.shotclock.budzik.MainActivity
import pl.shotclock.budzik.data.Alarm

/**
 * Ustawia i odwołuje alarmy w systemie. Repozytorium zna tylko ten interfejs,
 * więc w testach jednostkowych podstawiamy prostą atrapę zamiast prawdziwego AlarmManagera.
 */
interface AlarmScheduler {
    /** Ustawia [alarm] na najbliższy termin po chwili [from]. Poprzedni termin tego alarmu zostaje zastąpiony. */
    fun schedule(alarm: Alarm, from: LocalDateTime)

    fun cancel(alarmId: Long)
}

/**
 * Prawdziwy scheduler oparty na AlarmManager.setAlarmClock.
 * To najbardziej niezawodny rodzaj alarmu: działa w trybie oszczędzania energii (Doze),
 * a system pokazuje ikonę budzika na pasku stanu.
 */
class AndroidAlarmScheduler(private val context: Context) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(alarm: Alarm, from: LocalDateTime) {
        // Bez zgody na dokładne alarmy (Android 12) system rzuciłby wyjątek. Karta na liście alarmów prosi o tę zgodę,
        // a po jej udzieleniu BootReceiver ustawi alarmy jeszcze raz.
        if (!AlarmPermissions.canScheduleExactAlarms(context)) return

        val ringTime = nextRingTime(alarm, from)
        val info = AlarmManager.AlarmClockInfo(
            ringTimeMillis(ringTime, ZoneId.systemDefault()),
            // Co otworzyć po kliknięciu ikony budzika w systemie: listę alarmów.
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        alarmManager.setAlarmClock(info, ringIntent(alarm.id))
    }

    override fun cancel(alarmId: Long) {
        alarmManager.cancel(ringIntent(alarmId))
    }

    // Id alarmu jest też numerem PendingIntentu, więc każdy alarm ma własny termin w systemie
    // i można go odwołać, nie ruszając pozostałych.
    private fun ringIntent(alarmId: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            Intent(context, AlarmReceiver::class.java).putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
