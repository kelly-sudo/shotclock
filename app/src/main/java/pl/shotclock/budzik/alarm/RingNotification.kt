package pl.shotclock.budzik.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import pl.shotclock.budzik.R
import pl.shotclock.budzik.RingActivity

/** Kanał i treść powiadomienia, które pokazuje się, gdy alarm dzwoni. */
object RingNotification {
    const val NOTIFICATION_ID = 1
    private const val CHANNEL_ID = "ringing_alarms"

    /** Kanał trzeba utworzyć raz, przy starcie aplikacji. Ponowne utworzenie nic nie zmienia. */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.ring_channel_name),
            // Wysoka ważność: powiadomienie może otworzyć ekran dzwonienia na pełnym ekranie.
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.ring_channel_description)
            // Dźwięk i wibracje odtwarza serwis, więc samo powiadomienie ma być ciche.
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun build(context: Context, label: String): Notification {
        // Ten sam ekran otwiera się sam (full-screen intent) albo po kliknięciu powiadomienia,
        // gdy ktoś akurat używa telefonu i system pokazuje tylko pasek u góry.
        val openRingScreen = PendingIntent.getActivity(
            context,
            0,
            Intent(context, RingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(context.getString(R.string.ring_status))
            .setContentText(label.ifBlank { context.getString(R.string.ring_notification_text) })
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Powiadomienia nie da się odsunąć: zniknie dopiero po wyłączeniu alarmu.
            .setOngoing(true)
            .setFullScreenIntent(openRingScreen, true)
            .setContentIntent(openRingScreen)
            .addAction(0, context.getString(R.string.ring_notification_open), openRingScreen)
            .build()
    }
}
