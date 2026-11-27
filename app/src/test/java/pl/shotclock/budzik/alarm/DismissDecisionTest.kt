package pl.shotclock.budzik.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Kiedy alarm można wyłączyć: przedmiot + próg 0.85 + utrzymanie wyniku przez 1 sekundę. */
class DismissDecisionTest {

    /** Kilka kolejnych klatek: para (czas w ms, pewność szukanego przedmiotu). Zwraca decyzję po ostatniej. */
    private fun decideFrames(vararg frames: Pair<Long, Float>, otherObject: Float = 0f): DismissDecision {
        var since: Long? = null
        var decision: DismissDecision? = null
        for ((time, confidence) in frames) {
            decision = decideDismiss(confidence, otherObject, nowMillis = time, matchSinceMillis = since)
            since = decision.matchSinceMillis
        }
        return decision!!
    }

    @Test
    fun lowConfidence_saysObjectIsNotVisible() {
        val decision = decideFrames(0L to 0.1f)
        assertEquals(RecognitionHint.NotVisible, decision.hint)
        assertFalse(decision.shouldDismiss)
        assertNull(decision.matchSinceMillis)
    }

    @Test
    fun mediumConfidence_asksToMoveCloser() {
        assertEquals(RecognitionHint.MoveCloser, decideFrames(0L to 0.6f).hint)
    }

    @Test
    fun otherObjectAboveThreshold_saysItIsTheWrongObject() {
        val decision = decideFrames(0L to 0.05f, otherObject = 0.92f)
        assertEquals(RecognitionHint.WrongObject, decision.hint)
    }

    @Test
    fun firstFrameAboveThreshold_waitsInsteadOfDismissing() {
        val decision = decideFrames(5_000L to 0.9f)
        assertEquals(RecognitionHint.HoldStill, decision.hint)
        assertEquals(5_000L, decision.matchSinceMillis)
        assertFalse(decision.shouldDismiss)
    }

    @Test
    fun aboveThresholdForOneSecond_dismisses() {
        val decision = decideFrames(0L to 0.9f, 400L to 0.88f, 800L to 0.95f, 1_200L to 0.9f)
        assertTrue(decision.shouldDismiss)
        assertEquals(RecognitionHint.Recognized, decision.hint)
    }

    @Test
    fun justUnderOneSecond_stillWaits() {
        val decision = decideFrames(0L to 0.9f, 400L to 0.9f, 999L to 0.9f)
        assertEquals(RecognitionHint.HoldStill, decision.hint)
    }

    @Test
    fun exactlyOneSecond_dismisses() {
        assertTrue(decideFrames(0L to 0.9f, 1_000L to 0.9f).shouldDismiss)
    }

    @Test
    fun exactlyAtThreshold_counts() {
        assertTrue(decideFrames(0L to 0.85f, 1_000L to 0.85f).shouldDismiss)
    }

    @Test
    fun dropBelowThreshold_restartsTheTimer() {
        // Spadek w 800 ms zeruje licznik, więc w 1200 ms mija dopiero 0,4 s od nowego startu.
        val decision = decideFrames(0L to 0.9f, 400L to 0.9f, 800L to 0.5f, 1_200L to 0.9f, 1_600L to 0.9f)
        assertEquals(RecognitionHint.HoldStill, decision.hint)
        assertEquals(1_200L, decision.matchSinceMillis)
    }

    @Test
    fun customThresholdAndHold_areRespected() {
        val decision = decideDismiss(
            requiredConfidence = 0.7f,
            otherObjectConfidence = 0f,
            nowMillis = 500L,
            matchSinceMillis = 0L,
            threshold = 0.6f,
            holdMillis = 500L,
        )
        assertTrue(decision.shouldDismiss)
    }

    @Test
    fun photo_isAcceptedOnlyAboveThreshold() {
        assertTrue(isPhotoAccepted(0.9f))
        assertTrue(isPhotoAccepted(0.85f))
        assertFalse(isPhotoAccepted(0.84f))
    }
}
