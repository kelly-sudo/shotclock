package pl.shotclock.budzik.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Wpisywanie i sprawdzanie wyniku na ekranie wyjścia awaryjnego. */
class EmergencyViewModelTest {
    private val viewModel = EmergencyViewModel()

    private fun type(text: String) = text.forEach { viewModel.onDigit(it.digitToInt()) }

    // Wynik o jeden większy jest na pewno zły i nadal ma najwyżej 3 cyfry (najwyżej 541).
    private fun wrongAnswer() = (viewModel.uiState.value.task.answer + 1).toString()

    @Test
    fun digitsAreAddedAndDeleted() {
        type("12")
        viewModel.onDelete()

        assertEquals("1", viewModel.uiState.value.answer)
    }

    @Test
    fun answerHasAtMostThreeDigits() {
        type("12345")

        assertEquals("123", viewModel.uiState.value.answer)
    }

    @Test
    fun correctAnswerIsAccepted() {
        type(viewModel.uiState.value.task.answer.toString())

        assertTrue(viewModel.onSubmit())
    }

    @Test
    fun wrongAnswerShowsMessageAndClearsInput() {
        type(wrongAnswer())

        assertFalse(viewModel.onSubmit())
        assertTrue(viewModel.uiState.value.wrongAnswer)
        assertEquals("", viewModel.uiState.value.answer)
    }

    @Test
    fun emptyAnswerIsIgnored() {
        val task = viewModel.uiState.value.task

        assertFalse(viewModel.onSubmit())
        assertFalse(viewModel.uiState.value.wrongAnswer)
        assertEquals(task, viewModel.uiState.value.task)
    }

    @Test
    fun typingAfterWrongAnswerHidesTheMessage() {
        type(wrongAnswer())
        viewModel.onSubmit()

        type("4")

        assertFalse(viewModel.uiState.value.wrongAnswer)
        assertEquals("4", viewModel.uiState.value.answer)
    }
}
