package pl.shotclock.budzik.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Testy zapytań do tabeli z historią pobudek. Uruchamiają się na emulatorze lub telefonie. */
@RunWith(AndroidJUnit4::class)
class WakeEventDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: WakeEventDao

    private val recognizedWake = WakeEvent(
        alarmId = 3,
        ringStartedAt = 1_000,
        dismissedAt = 103_000,
        attempts = 2,
        recognizedObject = RequiredObject.SECOND,
        confidence = 0.87f,
        emergency = false,
    )

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Baza w pamięci znika po teście, więc każdy test zaczyna od pustej tabeli.
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.wakeEventDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun recognizedWakeIsReadBackWithAllFields() = runTest {
        val id = dao.insert(recognizedWake)

        assertEquals(listOf(recognizedWake.copy(id = id)), dao.getAll())
    }

    @Test
    fun emergencyWakeIsSavedWithoutObjectAndConfidence() = runTest {
        val emergencyWake = recognizedWake.copy(
            dismissedAt = 601_000,
            attempts = 10,
            recognizedObject = null,
            confidence = null,
            emergency = true,
        )

        val id = dao.insert(emergencyWake)

        assertEquals(emergencyWake.copy(id = id), dao.getAll().single())
    }

    @Test
    fun wakesAreSortedFromNewest() = runTest {
        val olderId = dao.insert(recognizedWake.copy(dismissedAt = 1_000))
        val newerId = dao.insert(recognizedWake.copy(dismissedAt = 2_000))

        assertEquals(listOf(newerId, olderId), dao.getAll().map { it.id })
    }

    @Test
    fun observedListContainsNewWakes() = runTest {
        // Ekran historii słucha tej listy, więc po zapisie musi dostać nową pobudkę.
        val id = dao.insert(recognizedWake)

        assertEquals(listOf(id), dao.observeAll().first().map { it.id })
    }
}
