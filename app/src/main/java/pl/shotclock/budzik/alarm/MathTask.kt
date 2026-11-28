package pl.shotclock.budzik.alarm

import kotlin.random.Random

/**
 * Zadanie matematyczne z wyjścia awaryjnego: [first] × [second] + [third], np. 23 × 7 + 48.
 * Na tyle trudne, że trzeba się obudzić, i na tyle łatwe, że da się je policzyć w pamięci.
 */
data class MathTask(val first: Int, val second: Int, val third: Int) {
    val answer: Int get() = first * second + third
}

/** Losuje nowe zadanie. [random] podajemy w testach, żeby wynik był powtarzalny. */
fun createMathTask(random: Random = Random.Default): MathTask =
    MathTask(
        first = random.nextInt(from = 12, until = 50),
        second = random.nextInt(from = 3, until = 10),
        third = random.nextInt(from = 10, until = 100),
    )

/** Czy wpisany tekst to poprawny wynik. Pusty albo nieliczbowy tekst to zła odpowiedź. */
fun isCorrectAnswer(task: MathTask, input: String): Boolean =
    input.trim().toIntOrNull() == task.answer
