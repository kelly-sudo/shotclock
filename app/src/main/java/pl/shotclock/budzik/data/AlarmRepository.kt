package pl.shotclock.budzik.data

import kotlinx.coroutines.flow.Flow

/**
 * Jedyne miejsce, przez które ViewModele czytają i zapisują alarmy. Ekrany nie wiedzą nic o bazie.
 * W Etapie 3 dojdzie tu ustawianie alarmu w systemie (AlarmManager) przy każdej zmianie.
 */
class AlarmRepository(private val alarmDao: AlarmDao) {
    val alarms: Flow<List<Alarm>> = alarmDao.observeAll()

    suspend fun getAlarm(id: Long): Alarm? = alarmDao.getById(id)

    /** Zapisuje nowy alarm albo zmiany w istniejącym i zwraca jego id. */
    suspend fun save(alarm: Alarm): Long =
        if (alarm.id == 0L) {
            alarmDao.insert(alarm)
        } else {
            alarmDao.update(alarm)
            alarm.id
        }

    suspend fun setEnabled(id: Long, enabled: Boolean) = alarmDao.setEnabled(id, enabled)

    suspend fun delete(alarm: Alarm) = alarmDao.delete(alarm)

    /** Przywraca usunięty alarm z tym samym id (przycisk „Cofnij”). */
    suspend fun restore(alarm: Alarm) {
        alarmDao.insert(alarm)
    }
}
