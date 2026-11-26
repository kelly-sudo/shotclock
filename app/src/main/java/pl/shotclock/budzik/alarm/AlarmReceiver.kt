package pl.shotclock.budzik.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** System budzi go o godzinie alarmu. Uruchamia serwis, który dzwoni i pokazuje ekran dzwonienia. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, NO_ALARM)
        if (alarmId == NO_ALARM) return
        RingService.start(context, alarmId)
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        private const val NO_ALARM = -1L
    }
}
