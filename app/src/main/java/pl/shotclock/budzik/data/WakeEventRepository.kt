package pl.shotclock.budzik.data

import java.time.ZoneId

/**
 * Jedyne miejsce, przez które ViewModele zapisują i czytają historię pobudek.
 * [zone] podajemy w testach, żeby wynik nie zależał od strefy czasowej komputera.
 */
class WakeEventRepository(
    private val wakeEventDao: WakeEventDao,
    private val zone: () -> ZoneId = ZoneId::systemDefault,
) {
    /** Zapisuje pobudkę w bazie i zwraca jej podsumowanie do ekranu sukcesu (czas wstania, seria dni). */
    suspend fun recordWake(event: WakeEvent): WakeSummary {
        val id = wakeEventDao.insert(event)
        return summarizeWake(event.copy(id = id), wakeEventDao.getAll(), zone())
    }
}
