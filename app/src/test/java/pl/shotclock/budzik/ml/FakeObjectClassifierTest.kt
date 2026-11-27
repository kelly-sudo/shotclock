package pl.shotclock.budzik.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Udawany model: ciemno albo pusto → „inne”, dużo szczegółów → szukany przedmiot. */
class FakeObjectClassifierTest {

    private fun gray(value: Int): Int = (0xFF shl 24) or (value shl 16) or (value shl 8) or value

    @Test
    fun frameStats_ofUniformImage_hasNoDetail() {
        val stats = FrameStats.of(IntArray(100) { gray(120) })
        assertEquals(120f, stats.brightness, 0.01f)
        assertEquals(0f, stats.detail, 0.01f)
    }

    @Test
    fun frameStats_ofHalfBlackHalfWhite_hasMaximumDetail() {
        val stats = FrameStats.of(IntArray(100) { if (it < 50) gray(0) else gray(255) })
        assertEquals(127.5f, stats.brightness, 0.01f)
        assertEquals(127.5f, stats.detail, 0.01f)
    }

    @Test
    fun darkImage_isOther() {
        val results = fakeResults(FrameStats(brightness = 10f, detail = 60f), ModelLabels.FIRST)
        assertEquals(ModelLabels.OTHER, results.first().label)
        assertTrue(results.confidenceOf(ModelLabels.FIRST) < 0.1f)
    }

    @Test
    fun flatImage_isOther() {
        val results = fakeResults(FrameStats(brightness = 150f, detail = 5f), ModelLabels.SECOND)
        assertEquals(ModelLabels.OTHER, results.first().label)
    }

    @Test
    fun detailedImage_seesTheRequestedObjectAboveThreshold() {
        val results = fakeResults(FrameStats(brightness = 120f, detail = 60f), ModelLabels.SECOND)
        assertEquals(ModelLabels.SECOND, results.first().label)
        assertTrue(results.confidenceOf(ModelLabels.SECOND) > 0.85f)
    }

    @Test
    fun someDetail_givesMediumConfidence() {
        val confidence = fakeResults(FrameStats(brightness = 120f, detail = 25f), ModelLabels.FIRST)
            .confidenceOf(ModelLabels.FIRST)
        assertTrue(confidence in 0.5f..0.85f)
    }

    @Test
    fun results_coverAllLabelsAndSumToOne() {
        val results = fakeResults(FrameStats(brightness = 120f, detail = 30f), ModelLabels.FIRST)
        assertEquals(ModelLabels.all.toSet(), results.map { it.label }.toSet())
        assertEquals(1f, results.sumOf { it.confidence.toDouble() }.toFloat(), 0.001f)
    }
}
