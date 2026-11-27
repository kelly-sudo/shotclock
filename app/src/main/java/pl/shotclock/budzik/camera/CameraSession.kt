package pl.shotclock.budzik.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Zdjęcie do rozpoznania nie musi mieć pełnej rozdzielczości aparatu. Mniejsze szybciej się przetwarza.
private val PhotoSize = Size(1280, 720)

/**
 * Cała obsługa CameraX w jednym miejscu: podgląd, analiza klatek na żywo i zdjęcie w pamięci.
 * Ekran tylko pokazuje podgląd z [controller] i woła [start], [stop] i [takePhoto].
 */
class CameraSession(
    private val context: Context,
    onFrame: (image: Bitmap, timeMillis: Long) -> Unit,
) {
    // Osobny wątek w tle dla klatek i zdjęć, żeby nie zacinać ekranu.
    private val backgroundExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    val controller = LifecycleCameraController(context).apply {
        setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
        // RGBA od razu nadaje się na Bitmap, bez przeliczania kolorów z formatu kamery.
        imageAnalysisOutputImageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
        // Gdy analiza nie nadąża, bierzemy zawsze najnowszą klatkę, a starsze przepadają.
        imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        imageCaptureResolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(PhotoSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER),
            )
            .build()
        setImageAnalysisAnalyzer(backgroundExecutor, FrameAnalyzer(onFrame))
    }

    /** Włącza kamerę. CameraX sam ją wyłączy i włączy, gdy ekran zniknie i wróci. */
    fun start(lifecycleOwner: LifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        controller.initializationFuture.addListener(::chooseAvailableCamera, ContextCompat.getMainExecutor(context))
    }

    /** Wyłącza kamerę i wątek w tle. Wołane, gdy ekran kamery znika na dobre. */
    fun stop() {
        controller.clearImageAnalysisAnalyzer()
        controller.unbind()
        backgroundExecutor.shutdown()
    }

    /**
     * Robi zdjęcie i oddaje je jako Bitmap. Zdjęcie zostaje tylko w pamięci (prywatność), nie trafia do galerii.
     * [onPhoto] i [onError] są wywoływane w wątku w tle.
     */
    fun takePhoto(onPhoto: (Bitmap) -> Unit, onError: () -> Unit) {
        controller.takePicture(
            backgroundExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.use { it.toUprightBitmap() }
                    onPhoto(bitmap)
                }

                override fun onError(exception: ImageCaptureException) = onError()
            },
        )
    }

    /** Domyślnie tylna kamera. Gdy jej nie ma (np. kamera laptopa w emulatorze), używamy przedniej. */
    private fun chooseAvailableCamera() {
        if (!controller.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) &&
            controller.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        ) {
            controller.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }
}
