package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.shotclock.budzik.alarm.MathTask
import pl.shotclock.budzik.alarm.createMathTask
import pl.shotclock.budzik.alarm.isCorrectAnswer

// Wynik zadania ma najwyżej 3 cyfry (49 × 9 + 99 = 540), więc dłuższego tekstu nie przyjmujemy.
private const val MaxAnswerDigits = 3

/** Wszystko, co pokazuje ekran wyjścia awaryjnego. */
data class EmergencyUiState(
    val task: MathTask,
    // Wpisany wynik, cyfra po cyfrze.
    val answer: String = "",
    // true po złej odpowiedzi: pokazujemy komunikat i nowe zadanie.
    val wrongAnswer: Boolean = false,
)

/** Zadanie matematyczne, które wyłącza alarm, gdy przedmiotu nie da się znaleźć. */
class EmergencyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyUiState(task = createMathTask()))
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    fun onDigit(digit: Int) {
        _uiState.update {
            if (it.answer.length >= MaxAnswerDigits) it else it.copy(answer = it.answer + digit, wrongAnswer = false)
        }
    }

    fun onDelete() {
        _uiState.update { it.copy(answer = it.answer.dropLast(1)) }
    }

    /**
     * Sprawdza wynik. Zwraca true, gdy jest poprawny (wtedy ekran wyłącza alarm).
     * Po złej odpowiedzi losujemy nowe zadanie, żeby nie dało się zgadywać tej samej liczby.
     */
    fun onSubmit(): Boolean {
        val state = _uiState.value
        // Puste pole to jeszcze nie odpowiedź, więc nie losujemy nowego zadania.
        if (state.answer.isEmpty()) return false
        if (isCorrectAnswer(state.task, state.answer)) return true
        _uiState.value = EmergencyUiState(task = createMathTask(), wrongAnswer = true)
        return false
    }
}
