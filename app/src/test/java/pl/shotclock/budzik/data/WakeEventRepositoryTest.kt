package pl.shotclock.budzik.data

import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/** Repozytorium zapisuje pobudkę i od razu zwraca podsumowanie dla ekranu sukcesu. */
class WakeEventRepositoryTest {
    private val dao = FakeWakeEventDao()
    private val repository = WakeEventRepository(dao, zone = { ZoneId.of("UTC") })

    private fun wake(dismissedAt: Long, emergency: Boolean = false) = WakeEvent(
        alarmId = 7,
        ringStartedAt = dismissedAt - 90_000,
        dismissedAt = dismissedAt,
        attempts = 2,
        recognizedObject = if (emergency) null else RequiredObject.SECOND,
        confidence = if (emergency) null else 0.8f,
        emergency = emergency,
    )

    @Test
    fun recordedWakeIsSavedWithNewId() = runBlocking {
        val summary = repository.recordWake(wake(dismissedAt = DAY))

        assertEquals(1, dao.events.size)
        assertEquals(1L, summary.event.id)
        assertEquals(90, summary.wakeUpSeconds)
    }

    @Test
    fun streakIncludesTheWakeJustRecorded() = runBlocking {
        repository.recordWake(wake(dismissedAt = DAY))

        val summary = repository.recordWake(wake(dismissedAt = 2 * DAY))

        assertEquals(2, summary.streakDays)
    }

    @Test
    fun emergencyWakeResetsTheStreak() = runBlocking {
        repository.recordWake(wake(dismissedAt = DAY))

        val summary = repository.recordWake(wake(dismissedAt = 2 * DAY, emergency = true))

        assertEquals(0, summary.streakDays)
    }

    private companion object {
        // Doba w milisekundach. Pobudki co dobę to kolejne dni.
        const val DAY = 24 * 60 * 60 * 1000L
    }
}

/** Udawana tabela pobudek w pamięci, żeby testy działały bez telefonu. */
private class FakeWakeEventDao : WakeEventDao {
    val events = mutableListOf<WakeEvent>()

    override suspend fun insert(event: WakeEvent): Long {
        val id = events.size + 1L
        events += event.copy(id = id)
        return id
    }

    override suspend fun getAll(): List<WakeEvent> = events.sortedByDescending { it.dismissedAt }

    override fun observeAll(): Flow<List<WakeEvent>> = flowOf(events.sortedByDescending { it.dismissedAt })
}
