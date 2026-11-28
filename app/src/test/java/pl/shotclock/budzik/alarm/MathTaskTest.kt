package pl.shotclock.budzik.alarm

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Zadanie matematyczne z wyjścia awaryjnego. */
class MathTaskTest {
    @Test
    fun answerIsMultiplicationThenAddition() {
        assertEquals(209, MathTask(first = 23, second = 7, third = 48).answer)
    }

    @Test
    fun correctAnswerIsAccepted() {
        assertTrue(isCorrectAnswer(MathTask(23, 7, 48), "209"))
    }

    @Test
    fun spacesAroundAnswerDoNotMatter() {
        assertTrue(isCorrectAnswer(MathTask(23, 7, 48), " 209 "))
    }

    @Test
    fun wrongEmptyOrNonNumericAnswerIsRejected() {
        val task = MathTask(23, 7, 48)

        assertFalse(isCorrectAnswer(task, "208"))
        assertFalse(isCorrectAnswer(task, ""))
        assertFalse(isCorrectAnswer(task, "abc"))
    }

    @Test
    fun randomTasksStayInRangeAndHaveAtMostThreeDigitAnswers() {
        val random = Random(seed = 42)
        repeat(1_000) {
            val task = createMathTask(random)

            assertTrue(task.first in 12..49)
            assertTrue(task.second in 3..9)
            assertTrue(task.third in 10..99)
            assertTrue(task.answer in 46..540)
        }
    }

    @Test
    fun sameSeedGivesSameTask() {
        assertEquals(createMathTask(Random(seed = 7)), createMathTask(Random(seed = 7)))
    }
}
