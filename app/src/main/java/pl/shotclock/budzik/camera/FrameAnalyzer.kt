package pl.shotclock.budzik.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/** Co tyle milisekund analizujemy klatkę. Częściej nie ma sensu, a starszy telefon by się grzał. */
const val AnalysisIntervalMillis = 400L

/**
 * Dostaje klatki z kamery (CameraX wywołuje go w tle), przepuszcza tylko jedną na [AnalysisIntervalMillis]
 * i oddaje ją jako obrócony „do pionu” obraz. Obraz żyje tylko w pamięci, nic nie zapisujemy na dysku.
 */
class FrameAnalyzer(
    private val onFrame: (image: Bitmap, timeMillis: Long) -> Unit,
) : ImageAnalysis.Analyzer {
    private var lastAnalyzedMillis = 0L

    override fun analyze(image: ImageProxy) {
        // use { } zamyka klatkę na końcu. Bez tego CameraX przestałby przysyłać kolejne.
        image.use {
            val now = SystemClock.elapsedRealtime()
            if (now - lastAnalyzedMillis < AnalysisIntervalMillis) return
            lastAnalyzedMillis = now
            onFrame(it.toUprightBitmap(), now)
        }
    }
}

/** Zamienia klatkę lub zdjęcie z CameraX na Bitmap ustawiony tak, jak trzyma się telefon. */
fun ImageProxy.toUprightBitmap(): Bitmap {
    val bitmap = toBitmap()
    val rotation = imageInfo.rotationDegrees
    if (rotation == 0) return bitmap
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
