package pl.shotclock.budzik.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.shotclock.budzik.alarm.DefaultConfidenceThreshold

/** Przeliczenie podobieństwa z modelu na skalę aplikacji: próg modelu ma wypaść dokładnie w progu aplikacji. */
class ScoreScaleTest {

    private val modelThreshold = 0.8f

    @Test
    fun modelThreshold_becomesAppThreshold() {
        assertEquals(DefaultConfidenceThreshold, similarityToConfidence(0.8f, modelThreshold), 0.0001f)
    }

    @Test
    fun ends_stayAtZeroAndOne() {
        assertEquals(0f, similarityToConfidence(0f, modelThreshold), 0.0001f)
        assertEquals(1f, similarityToConfidence(1f, modelThreshold), 0.0001f)
    }

    @Test
    fun belowModelThreshold_isBelowAppThreshold() {
        assertTrue(similarityToConfidence(0.79f, modelThreshold) < DefaultConfidenceThreshold)
    }

    @Test
    fun aboveModelThreshold_isAboveAppThreshold() {
        assertTrue(similarityToConfidence(0.81f, modelThreshold) > DefaultConfidenceThreshold)
    }

    @Test
    fun valuesOutsideRange_areClamped() {
        assertEquals(0f, similarityToConfidence(-0.3f, modelThreshold), 0.0001f)
        assertEquals(1f, similarityToConfidence(1.4f, modelThreshold), 0.0001f)
    }
}
