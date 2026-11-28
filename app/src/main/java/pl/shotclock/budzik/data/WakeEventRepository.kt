package pl.shotclock.budzik.data

import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

/**
 * Jedyne miejsce, przez które ViewModele zapisują i czytają historię pobudek.
 * [zone] podajemy w testach, żeby wynik nie zależał od strefy czasowej komputera.
 */
class WakeEventRepository(
    private val wakeEventDao: WakeEventDao,
    private val zone: () -> ZoneId = ZoneId::systemDefault,
) {
    /** Wszystkie pobudki, od najnowszej. Nowa lista przychodzi po każdym zapisie. */
    val events: Flow<List<WakeEvent>> = wakeEventDao.observeAll()

    /** Zapisuje pobudkę w bazie i zwraca jej podsumowanie do ekranu sukcesu (czas wstania, seria dni). */
    suspend fun recordWake(event: WakeEvent): WakeSummary {
        val id = wakeEventDao.insert(event)
        return summarizeWake(event.copy(id = id), wakeEventDao.getAll(), zone())
    }
}
