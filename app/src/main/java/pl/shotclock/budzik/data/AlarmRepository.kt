package pl.shotclock.budzik.data

import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import pl.shotclock.budzik.alarm.AlarmScheduler

/**
 * Jedyne miejsce, przez które ViewModele czytają i zapisują alarmy. Ekrany nie wiedzą nic o bazie.
 * Po każdej zmianie w bazie od razu ustawia albo odwołuje alarm w systemie, więc baza i system się nie rozjeżdżają.
 * [now] podajemy w testach, żeby sprawdzić planowanie dla wybranej godziny.
 */
class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val scheduler: AlarmScheduler,
    private val now: () -> LocalDateTime = LocalDateTime::now,
) {
    val alarms: Flow<List<Alarm>> = alarmDao.observeAll()

    suspend fun getAlarm(id: Long): Alarm? = alarmDao.getById(id)

    /** Zapisuje nowy alarm albo zmiany w istniejącym i zwraca jego id. */
    suspend fun save(alarm: Alarm): Long {
        val id = if (alarm.id == 0L) {
            alarmDao.insert(alarm)
        } else {
            alarmDao.update(alarm)
            alarm.id
        }
        updateSchedule(alarm.copy(id = id))
        return id
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) {
        alarmDao.setEnabled(id, enabled)
        alarmDao.getById(id)?.let { updateSchedule(it) }
    }

    suspend fun delete(alarm: Alarm) {
        alarmDao.delete(alarm)
        scheduler.cancel(alarm.id)
    }

    /** Przywraca usunięty alarm z tym samym id (przycisk „Cofnij”). */
    suspend fun restore(alarm: Alarm) {
        alarmDao.insert(alarm)
        updateSchedule(alarm)
    }

    /** Ustawia w systemie wszystkie alarmy od nowa, np. po restarcie telefonu albo zmianie strefy czasowej. */
    suspend fun rescheduleAll() {
        alarmDao.getAll().forEach { updateSchedule(it) }
    }

    /**
     * Alarm właśnie zaczął dzwonić. Jednorazowy wyłączamy, a powtarzany ustawiamy na kolejny pasujący dzień.
     * Liczymy od chwili o minutę późniejszej, żeby ta sama minuta nie została zaplanowana drugi raz.
     */
    suspend fun onAlarmRang(alarm: Alarm) {
        if (alarm.days.isEmpty()) {
            alarmDao.setEnabled(alarm.id, false)
            scheduler.cancel(alarm.id)
        } else {
            scheduler.schedule(alarm, from = now().plusMinutes(1))
        }
    }

    private fun updateSchedule(alarm: Alarm) {
        if (alarm.enabled) scheduler.schedule(alarm, from = now()) else scheduler.cancel(alarm.id)
    }
}
