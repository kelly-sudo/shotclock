package pl.shotclock.budzik.alarm

/**
 * Domyślny próg pewności: 55%, czyli środek przedziału 50–60% ustalonego z prowadzącym.
 * W Etapie 6 da się go zmienić w ustawieniach deweloperskich.
 */
const val DefaultConfidenceThreshold = 0.55f

/** Tak długo pewność musi zostać nad progiem, żeby alarm się wyłączył. Chroni przed przypadkowym „mignięciem”. */
const val DefaultHoldMillis = 1_000L

// Poniżej tej pewności mówimy, że przedmiotu w ogóle nie widać.
private const val SeenThreshold = 0.3f

/** Podpowiedź pokazywana na ekranie kamery. */
enum class RecognitionHint {
    /** „Nie widzę przedmiotu”. */
    NotVisible,

    /** Kamera widzi inny przedmiot niż ten, którego szukamy. */
    WrongObject,

    /** Przedmiot jest, ale pewność jest jeszcze za mała: „Przybliż”. */
    MoveCloser,

    /** Pewność jest nad progiem, czekamy na utrzymanie wyniku: „Jeszcze chwilę…”. */
    HoldStill,

    /** Rozpoznano, alarm można wyłączyć. */
    Recognized,
}

/**
 * Wynik decyzji po jednej klatce.
 * [matchSinceMillis] to chwila, od której pewność jest nad progiem (null, gdy nie jest). Trzeba go podać przy następnej klatce.
 */
data class DismissDecision(
    val hint: RecognitionHint,
    val matchSinceMillis: Long?,
) {
    val shouldDismiss: Boolean get() = hint == RecognitionHint.Recognized
}

/**
 * Decyduje, czy alarm można wyłączyć. Czysta funkcja: nie zna Androida, kamery ani zegara,
 * wszystko dostaje w parametrach, więc łatwo ją sprawdzić testami (DismissDecisionTest).
 *
 * @param requiredConfidence pewność modelu dla przedmiotu, którego szukamy
 * @param otherObjectConfidence pewność dla drugiego przedmiotu (żeby podpowiedzieć „to nie ten”)
 * @param nowMillis czas tej klatki
 * @param matchSinceMillis wartość z poprzedniej decyzji
 */
fun decideDismiss(
    requiredConfidence: Float,
    otherObjectConfidence: Float,
    nowMillis: Long,
    matchSinceMillis: Long?,
    threshold: Float = DefaultConfidenceThreshold,
    holdMillis: Long = DefaultHoldMillis,
): DismissDecision {
    if (requiredConfidence < threshold) {
        // Pewność spadła pod próg, więc liczenie czasu zaczyna się od nowa.
        val hint = when {
            otherObjectConfidence >= threshold -> RecognitionHint.WrongObject
            requiredConfidence < SeenThreshold -> RecognitionHint.NotVisible
            else -> RecognitionHint.MoveCloser
        }
        return DismissDecision(hint, matchSinceMillis = null)
    }
    val since = matchSinceMillis ?: nowMillis
    val hint = if (nowMillis - since >= holdMillis) RecognitionHint.Recognized else RecognitionHint.HoldStill
    return DismissDecision(hint, matchSinceMillis = since)
}

/** Ręczne zdjęcie nie ma „utrzymania”: wystarczy, że jedno zdjęcie przekroczy próg. */
fun isPhotoAccepted(requiredConfidence: Float, threshold: Float = DefaultConfidenceThreshold): Boolean =
    requiredConfidence >= threshold
