package pl.shotclock.budzik.alarm

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.shotclock.budzik.ShotClockApplication
import pl.shotclock.budzik.data.Alarm

private const val VOLUME_STEP_MILLIS = 500L
// Wibracja: 0,8 s drgania, 0,6 s przerwy, w kółko.
private val VibrationPattern = longArrayOf(0, 800, 600)
private const val REPEAT_FROM_START = 0

/**
 * Serwis pierwszoplanowy, który dzwoni: odtwarza dźwięk alarmu (z narastającą głośnością), wibruje
 * i pokazuje powiadomienie otwierające ekran dzwonienia. Działa, dopóki alarm nie zostanie wyłączony,
 * także gdy ekran jest zgaszony. Drzemki celowo nie ma.
 */
class RingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val container get() = (application as ShotClockApplication).container

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var volumeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, NO_ALARM) ?: NO_ALARM
        scope.launch { startRinging(alarmId) }
        // Jeśli system zamknie serwis z braku pamięci, nie wznawiamy go sam: alarm i tak jest zapisany w bazie.
        return START_NOT_STICKY
    }

    private suspend fun startRinging(alarmId: Long) {
        val alarm = container.alarmRepository.getAlarm(alarmId)

        // Najpierw stan dzwonienia, potem powiadomienie: ekran dzwonienia otwierany z powiadomienia
        // musi już zastać dzwoniący alarm.
        if (alarm != null) {
            container.ringController.onRingingStarted(
                RingingAlarm(
                    alarm = alarm,
                    objectToFind = pickObjectToFind(alarm.requiredObject),
                    startedAtMillis = System.currentTimeMillis(),
                ),
            )
        }
        // Serwis uruchomiony przez startForegroundService musi szybko wywołać startForeground, nawet gdy zaraz skończy.
        ServiceCompat.startForeground(
            this,
            RingNotification.NOTIFICATION_ID,
            RingNotification.build(this, alarm?.label.orEmpty()),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0,
        )
        if (alarm == null) {
            // Alarm usunięto, zanim zadzwonił. Jeśli dzwoni inny alarm, niech dzwoni dalej.
            if (container.ringController.ringing.value == null) stopSelf()
            return
        }

        // Jednorazowy alarm się wyłącza, powtarzany dostaje następny termin.
        container.alarmRepository.onAlarmRang(alarm)

        stopSoundAndVibration()
        startSound(alarm)
        if (alarm.vibration) startVibration()
    }

    private fun startSound(alarm: Alarm) {
        // Kolejność dźwięków (easter egg o 21:37, wybrany dźwięk, domyślne) opisuje ringSoundCandidates.
        val candidates = ringSoundCandidates(
            alarm = alarm,
            easterEggSound = easterEggSoundUri(),
            defaultSounds = listOfNotNull(
                RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM),
                Settings.System.DEFAULT_ALARM_ALERT_URI,
                Settings.System.DEFAULT_RINGTONE_URI,
            ).map { it.toString() },
        ).map { it.toUri() }
        val newPlayer = candidates.firstNotNullOfOrNull { createPlayer(it) } ?: return
        player = newPlayer

        val startedAt = SystemClock.elapsedRealtime()
        volumeJob = scope.launch {
            while (true) {
                val volume = ringVolume(SystemClock.elapsedRealtime() - startedAt, alarm.risingVolume)
                newPlayer.setVolume(volume, volume)
                if (volume >= 1f) break
                delay(VOLUME_STEP_MILLIS)
            }
        }
        newPlayer.start()
    }

    /**
     * Adres pliku res/raw/easter_2137 albo null, gdy zespół go nie dodał.
     * Szukamy pliku po nazwie, bo zwykłe R.raw.easter_2137 nie skompiluje się, dopóki pliku nie ma.
     */
    @SuppressLint("DiscouragedApi")
    private fun easterEggSoundUri(): String? {
        val id = resources.getIdentifier(EASTER_EGG_SOUND_NAME, "raw", packageName)
        return if (id == 0) null else "android.resource://$packageName/$id"
    }

    /** Przygotowany odtwarzacz w pętli albo null, gdy tego dźwięku nie da się odtworzyć. */
    private fun createPlayer(uri: Uri): MediaPlayer? {
        val newPlayer = MediaPlayer()
        return try {
            newPlayer.apply {
                // USAGE_ALARM: dźwięk idzie głośnością alarmu i gra także w trybie „Nie przeszkadzać”, jeśli alarmy są dozwolone.
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(this@RingService, uri)
                isLooping = true
                // Procesor nie zaśnie w trakcie dzwonienia, nawet przy zgaszonym ekranie.
                setWakeMode(this@RingService, PowerManager.PARTIAL_WAKE_LOCK)
                prepare()
            }
        } catch (e: Exception) {
            newPlayer.release()
            null
        }
    }

    private fun startVibration() {
        val systemVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
        val effect = VibrationEffect.createWaveform(VibrationPattern, REPEAT_FROM_START)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            systemVibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
        } else {
            @Suppress("DEPRECATION")
            systemVibrator.vibrate(effect, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
        }
        vibrator = systemVibrator
    }

    private fun stopSoundAndVibration() {
        volumeJob?.cancel()
        player?.run {
            stop()
            release()
        }
        player = null
        vibrator?.cancel()
        vibrator = null
    }

    override fun onDestroy() {
        stopSoundAndVibration()
        scope.cancel()
        container.ringController.onRingingStopped()
        super.onDestroy()
    }

    companion object {
        private const val NO_ALARM = -1L

        fun start(context: Context, alarmId: Long) {
            val intent = Intent(context, RingService::class.java).putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
