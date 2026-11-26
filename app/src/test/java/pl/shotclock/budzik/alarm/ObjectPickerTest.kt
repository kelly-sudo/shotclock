package pl.shotclock.budzik.alarm

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.shotclock.budzik.data.RequiredObject

class ObjectPickerTest {

    @Test
    fun concreteObjectStaysTheSame() {
        assertEquals(RequiredObject.FIRST, pickObjectToFind(RequiredObject.FIRST))
        assertEquals(RequiredObject.SECOND, pickObjectToFind(RequiredObject.SECOND))
    }

    @Test
    fun randomNeverReturnsRandom() {
        val random = Random(seed = 42)
        repeat(100) {
            val picked = pickObjectToFind(RequiredObject.RANDOM, random)
            assertTrue(picked in listOf(RequiredObject.FIRST, RequiredObject.SECOND))
        }
    }

    @Test
    fun randomPicksBothObjectsOverTime() {
        val random = Random(seed = 7)
        val picked = List(100) { pickObjectToFind(RequiredObject.RANDOM, random) }.toSet()
        assertEquals(setOf(RequiredObject.FIRST, RequiredObject.SECOND), picked)
    }
}
