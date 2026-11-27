package pl.shotclock.budzik.ml

import android.graphics.Bitmap
import androidx.core.graphics.scale
import kotlin.math.sqrt

/**
 * Udawany model, dopóki zespół AI nie dostarczy prawdziwego (i później w trybie testowym).
 *
 * Nie rozpoznaje przedmiotów naprawdę. Patrzy tylko, czy obraz jest jasny i ma szczegóły:
 * - ciemny obraz albo jednolita powierzchnia (zasłonięta kamera, ściana) → „inne”,
 * - im więcej szczegółów, tym większa pewność, że widzi przedmiot [pretendToSee].
 * Dzięki temu na emulatorze i telefonie da się sprawdzić wszystkie podpowiedzi i wyłączenie alarmu.
 */
class FakeObjectClassifier(private val pretendToSee: String) : ObjectClassifier {

    override fun classify(image: Bitmap): List<ClassificationResult> {
        // Mały obraz wystarczy do policzenia jasności, a liczy się dużo szybciej (ważne na starszym Samsungu).
        val small = image.scale(SampleSize, SampleSize)
        val pixels = IntArray(SampleSize * SampleSize)
        small.getPixels(pixels, 0, SampleSize, 0, 0, SampleSize, SampleSize)
        if (small != image) small.recycle()
        return fakeResults(FrameStats.of(pixels), pretendToSee)
    }

    companion object {
        private const val SampleSize = 32
    }
}

/** Średnia jasność obrazu i to, jak bardzo jasność się zmienia (miara szczegółów). Obie od 0 do 255. */
data class FrameStats(val brightness: Float, val detail: Float) {
    companion object {
        /** Liczy statystyki z pikseli w formacie ARGB (tak jak zwraca je Bitmap.getPixels). */
        fun of(pixels: IntArray): FrameStats {
            if (pixels.isEmpty()) return FrameStats(0f, 0f)
            val lumas = pixels.map { luma(it) }
            val mean = lumas.average()
            val variance = lumas.sumOf { (it - mean) * (it - mean) } / lumas.size
            return FrameStats(mean.toFloat(), sqrt(variance).toFloat())
        }

        /** Jasność piksela tak, jak widzi ją oko: zielony liczy się najbardziej, niebieski najmniej. */
        private fun luma(argb: Int): Double {
            val red = (argb shr 16) and 0xFF
            val green = (argb shr 8) and 0xFF
            val blue = argb and 0xFF
            return 0.299 * red + 0.587 * green + 0.114 * blue
        }
    }
}

// Progi dobrane na oko na kamerze laptopa w emulatorze: pokój ma szczegóły około 40–60, zasłonięta kamera poniżej 10.
private const val MinBrightness = 35f
private const val MinDetail = 12f
private const val FullDetail = 45f
private const val LowestConfidence = 0.2f
private const val HighestConfidence = 0.97f

/** Czysta funkcja (bez Androida), żeby dało się ją sprawdzić testem. */
fun fakeResults(stats: FrameStats, pretendToSee: String): List<ClassificationResult> {
    val confidence = when {
        stats.brightness < MinBrightness || stats.detail < MinDetail -> 0.05f
        else -> {
            val progress = ((stats.detail - MinDetail) / (FullDetail - MinDetail)).coerceIn(0f, 1f)
            LowestConfidence + (HighestConfidence - LowestConfidence) * progress
        }
    }
    val others = ModelLabels.all.filter { it != pretendToSee && it != ModelLabels.OTHER }
    return buildList {
        add(ClassificationResult(pretendToSee, confidence))
        // Resztę pewności dostaje „inne”, a drugi przedmiot zawsze minimalnie, jak w prawdziwym modelu.
        val rest = 1f - confidence
        others.forEach { add(ClassificationResult(it, rest * 0.1f)) }
        add(ClassificationResult(ModelLabels.OTHER, rest * (if (others.isEmpty()) 1f else 0.9f)))
    }.sortedByDescending { it.confidence }
}
