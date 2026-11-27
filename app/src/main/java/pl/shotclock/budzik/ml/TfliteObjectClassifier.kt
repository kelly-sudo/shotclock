package pl.shotclock.budzik.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Prawdziwy model od zespołu AI (plik model.tflite w assets), uruchamiany na telefonie przez LiteRT.
 *
 * Rozmiar wejścia i typ danych (float32 albo uint8) odczytujemy z modelu, a nie wpisujemy na sztywno.
 * Model dostaje piksele w zakresie 0–255, bez normalizacji (tak go wytrenowano).
 */
class TfliteObjectClassifier private constructor(
    private val interpreter: Interpreter,
    private val labels: List<String>,
    private val modelThreshold: Float,
) : ObjectClassifier {

    private val inputShape = interpreter.getInputTensor(0).shape() // [1, wysokość, szerokość, 3]
    private val inputHeight = inputShape[1]
    private val inputWidth = inputShape[2]
    private val inputType = interpreter.getInputTensor(0).dataType()

    // synchronized: klatki na żywo i ręczne zdjęcie przychodzą z różnych wątków, a model obsłuży naraz tylko jedno.
    @Synchronized
    override fun classify(image: Bitmap): List<ClassificationResult> {
        val input = toInputBuffer(centerSquare(image).scale(inputWidth, inputHeight))
        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(input, output)
        return labels.mapIndexed { index, label ->
            ClassificationResult(label, similarityToConfidence(output[0][index], modelThreshold))
        }.sortedByDescending { it.confidence }
    }

    /** Środkowy kwadrat klatki, czyli to, co widać w celowniku. Model uczył się na zdjęciach, gdzie przedmiot wypełnia kadr. */
    private fun centerSquare(image: Bitmap): Bitmap {
        val side = minOf(image.width, image.height)
        return Bitmap.createBitmap(image, (image.width - side) / 2, (image.height - side) / 2, side, side)
    }

    private fun toInputBuffer(bitmap: Bitmap): ByteBuffer {
        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val bytesPerValue = if (inputType == DataType.UINT8) 1 else 4
        val buffer = ByteBuffer.allocateDirect(pixels.size * 3 * bytesPerValue).order(ByteOrder.nativeOrder())
        for (pixel in pixels) {
            val channels = intArrayOf((pixel shr 16) and 0xFF, (pixel shr 8) and 0xFF, pixel and 0xFF)
            for (value in channels) {
                if (inputType == DataType.UINT8) buffer.put(value.toByte()) else buffer.putFloat(value.toFloat())
            }
        }
        buffer.rewind()
        return buffer
    }

    companion object {
        private const val ModelFile = "model.tflite"
        private const val LabelsFile = "labels.txt"
        private const val ThresholdFile = "threshold.txt"
        private const val Tag = "TfliteObjectClassifier"

        /** Wczytuje model z assets albo zwraca null, gdy pliku nie ma lub jest uszkodzony (wtedy zostaje udawany model). */
        fun createIfAvailable(context: Context): TfliteObjectClassifier? {
            if (context.assets.list("")?.contains(ModelFile) != true) return null
            return try {
                val modelBytes = context.assets.open(ModelFile).use { it.readBytes() }
                val model = ByteBuffer.allocateDirect(modelBytes.size).order(ByteOrder.nativeOrder())
                model.put(modelBytes).rewind()
                val labels = context.assets.open(LabelsFile).bufferedReader().readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                val threshold = context.assets.open(ThresholdFile).bufferedReader().readText().trim().toFloat()
                TfliteObjectClassifier(Interpreter(model, Interpreter.Options().setNumThreads(2)), labels, threshold)
            } catch (e: Exception) {
                Log.e(Tag, "Nie udało się wczytać modelu, używam udawanego", e)
                null
            }
        }
    }
}
