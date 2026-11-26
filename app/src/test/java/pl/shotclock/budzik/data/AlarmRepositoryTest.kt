package pl.shotclock.budzik.data

import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.shotclock.budzik.alarm.AlarmScheduler

/** Sprawdza, czy repozytorium ustawia i odwołuje alarmy w systemie razem ze zmianami w bazie. */
class AlarmRepositoryTest {
    // 26 listopada 2026, czwartek, 22:00.
    private val now = LocalDateTime.of(2026, 11, 26, 22, 0)

    private val dao = FakeAlarmDao()
    private val scheduler = FakeAlarmScheduler()
    private val repository = AlarmRepository(dao, scheduler, now = { now })

    @Test
    fun savingEnabledAlarmSchedulesIt() = runBlocking {
        val id = repository.save(Alarm(hour = 7, minute = 0))

        assertEquals(id, scheduler.scheduled.getValue(id).alarm.id)
        assertEquals(now, scheduler.scheduled.getValue(id).from)
    }

    @Test
    fun disablingAlarmCancelsIt() = runBlocking {
        val id = repository.save(Alarm(hour = 7, minute = 0))

        repository.setEnabled(id, false)

        assertFalse(id in scheduler.scheduled)
    }

    @Test
    fun enablingAlarmSchedulesItAgain() = runBlocking {
        val id = repository.save(Alarm(hour = 7, minute = 0, enabled = false))
        assertFalse(id in scheduler.scheduled)

        repository.setEnabled(id, true)

        assertTrue(id in scheduler.scheduled)
    }

    @Test
    fun deletingAlarmCancelsItAndUndoSchedulesItAgain() = runBlocking {
        val id = repository.save(Alarm(hour = 7, minute = 0))
        val alarm = repository.getAlarm(id)!!

        repository.delete(alarm)
        assertFalse(id in scheduler.scheduled)

        repository.restore(alarm)
        assertTrue(id in scheduler.scheduled)
    }

    @Test
    fun oneTimeAlarmTurnsOffAfterRinging() = runBlocking {
        val id = repository.save(Alarm(hour = 7, minute = 0))

        repository.onAlarmRang(repository.getAlarm(id)!!)

        assertFalse(repository.getAlarm(id)!!.enabled)
        assertFalse(id in scheduler.scheduled)
    }

    @Test
    fun repeatingAlarmIsScheduledFromTheNextMinuteAfterRinging() = runBlocking {
        val id = repository.save(Alarm(hour = 7, minute = 0, days = setOf(DayOfWeek.MONDAY)))

        repository.onAlarmRang(repository.getAlarm(id)!!)

        assertTrue(repository.getAlarm(id)!!.enabled)
        assertEquals(now.plusMinutes(1), scheduler.scheduled.getValue(id).from)
    }

    @Test
    fun rescheduleAllSchedulesOnlyEnabledAlarms() = runBlocking {
        val enabledId = repository.save(Alarm(hour = 7, minute = 0))
        val disabledId = repository.save(Alarm(hour = 8, minute = 0, enabled = false))
        scheduler.scheduled.clear()

        repository.rescheduleAll()

        assertEquals(setOf(enabledId), scheduler.scheduled.keys)
        assertFalse(disabledId in scheduler.scheduled)
    }
}

/** Zapamiętuje, które alarmy są ustawione w „systemie”, zamiast korzystać z AlarmManagera. */
private class FakeAlarmScheduler : AlarmScheduler {
    data class Scheduled(val alarm: Alarm, val from: LocalDateTime)

    val scheduled = mutableMapOf<Long, Scheduled>()

    override fun schedule(alarm: Alarm, from: LocalDateTime) {
        scheduled[alarm.id] = Scheduled(alarm, from)
    }

    override fun cancel(alarmId: Long) {
        scheduled.remove(alarmId)
    }
}

/** Baza w zwykłej mapie, żeby test działał bez Androida. Prawdziwe zapytania sprawdza AlarmDaoTest. */
private class FakeAlarmDao : AlarmDao {
    private val alarms = MutableStateFlow<Map<Long, Alarm>>(emptyMap())
    private var nextId = 1L

    override fun observeAll(): Flow<List<Alarm>> = MutableStateFlow(alarms.value.values.toList())

    override suspend fun getAll(): List<Alarm> = alarms.value.values.toList()

    override suspend fun getById(id: Long): Alarm? = alarms.value[id]

    override suspend fun insert(alarm: Alarm): Long {
        val id = if (alarm.id == 0L) nextId++ else alarm.id
        alarms.value = alarms.value + (id to alarm.copy(id = id))
        return id
    }

    override suspend fun update(alarm: Alarm) {
        alarms.value = alarms.value + (alarm.id to alarm)
    }

    override suspend fun delete(alarm: Alarm) {
        alarms.value = alarms.value - alarm.id
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        val alarm = alarms.value[id] ?: return
        alarms.value = alarms.value + (id to alarm.copy(enabled = enabled))
    }
}
