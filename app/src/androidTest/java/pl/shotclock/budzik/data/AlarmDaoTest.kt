package pl.shotclock.budzik.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Testy zapytań do tabeli alarmów. Uruchamiają się na emulatorze lub telefonie. */
@RunWith(AndroidJUnit4::class)
class AlarmDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: AlarmDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Baza w pamięci znika po teście, więc każdy test zaczyna od pustej tabeli i nie rusza prawdziwych alarmów.
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.alarmDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertedAlarmIsReadBackWithAllFields() = runTest {
        val alarm = Alarm(
            hour = 7,
            minute = 30,
            days = setOf(MONDAY, FRIDAY),
            enabled = false,
            label = "Zajęcia",
            requiredObject = RequiredObject.SECOND,
            soundUri = "content://media/alarm/1",
            vibration = false,
            risingVolume = false,
        )

        val id = dao.insert(alarm)

        assertEquals(alarm.copy(id = id), dao.getById(id))
    }

    @Test
    fun alarmsAreSortedFromTheEarliestTime() = runTest {
        dao.insert(Alarm(hour = 9, minute = 0))
        dao.insert(Alarm(hour = 6, minute = 15))
        dao.insert(Alarm(hour = 6, minute = 5))

        val times = dao.observeAll().first().map { it.hour to it.minute }

        assertEquals(listOf(6 to 5, 6 to 15, 9 to 0), times)
    }

    @Test
    fun updateChangesTheSavedAlarm() = runTest {
        val id = dao.insert(Alarm(hour = 7, minute = 0, label = "Stara"))

        dao.update(Alarm(id = id, hour = 8, minute = 45, label = "Nowa"))

        val saved = dao.getById(id)!!
        assertEquals(8, saved.hour)
        assertEquals(45, saved.minute)
        assertEquals("Nowa", saved.label)
    }

    @Test
    fun setEnabledChangesOnlyTheChosenAlarm() = runTest {
        val firstId = dao.insert(Alarm(hour = 7, minute = 0, enabled = true))
        val secondId = dao.insert(Alarm(hour = 8, minute = 0, enabled = true))

        dao.setEnabled(firstId, enabled = false)

        assertFalse(dao.getById(firstId)!!.enabled)
        assertTrue(dao.getById(secondId)!!.enabled)
    }

    @Test
    fun deletedAlarmCanBeRestoredWithTheSameId() = runTest {
        val id = dao.insert(Alarm(hour = 7, minute = 0))
        val alarm = dao.getById(id)!!

        dao.delete(alarm)
        assertNull(dao.getById(id))

        dao.insert(alarm)
        assertEquals(alarm, dao.getById(id))
    }
}
