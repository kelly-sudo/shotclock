package pl.shotclock.budzik.ml

import android.graphics.Bitmap

/** Jedna klasa z modelu i to, jak bardzo model jest pewny, że widzi właśnie ją (od 0 do 1). */
data class ClassificationResult(
    val label: String,
    val confidence: Float,
)

/** Etykiety klas z kontraktu z zespołem AI (te same nazwy co w labels.txt). */
object ModelLabels {
    const val FIRST = "przedmiot_1"
    const val SECOND = "przedmiot_2"
    const val OTHER = "inne"

    val all = listOf(FIRST, SECOND, OTHER)
}

/**
 * Rozpoznawanie przedmiotu na zdjęciu. Reszta aplikacji zna tylko ten interfejs,
 * więc podmiana [FakeObjectClassifier] na prawdziwy model (Etap 8) nie zmienia ekranów ani ViewModeli.
 */
interface ObjectClassifier {
    /** Wyniki dla wszystkich klas, od najpewniejszej. Wywoływane w tle, nigdy w wątku ekranu. */
    fun classify(image: Bitmap): List<ClassificationResult>
}

/** Pewność dla jednej etykiety albo 0, gdy model jej nie zwrócił. */
fun List<ClassificationResult>.confidenceOf(label: String): Float =
    firstOrNull { it.label == label }?.confidence ?: 0f
