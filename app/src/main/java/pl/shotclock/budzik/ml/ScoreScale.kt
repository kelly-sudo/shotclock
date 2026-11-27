package pl.shotclock.budzik.ml

import pl.shotclock.budzik.alarm.DefaultConfidenceThreshold

/**
 * Model zespołu AI nie zwraca prawdopodobieństw, tylko podobieństwo do zdjęć przedmiotu (od 0 do 1)
 * i ma własny próg dobrany na zdjęciach walidacyjnych (plik threshold.txt).
 *
 * Ta funkcja przelicza podobieństwo na skalę aplikacji tak, żeby próg modelu wypadł dokładnie
 * w progu aplikacji (55%). Dzięki temu pasek pewności, podpowiedzi i decyzja o wyłączeniu alarmu
 * działają bez zmian. Pod progiem i nad progiem skala jest liniowa.
 *
 * Czysta funkcja (bez Androida), sprawdzana testem ScoreScaleTest.
 */
fun similarityToConfidence(
    similarity: Float,
    modelThreshold: Float,
    appThreshold: Float = DefaultConfidenceThreshold,
): Float {
    val score = similarity.coerceIn(0f, 1f)
    val confidence = if (score < modelThreshold) {
        appThreshold * score / modelThreshold
    } else {
        appThreshold + (1f - appThreshold) * (score - modelThreshold) / (1f - modelThreshold)
    }
    return confidence.coerceIn(0f, 1f)
}
