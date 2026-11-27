package pl.shotclock.budzik.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.shotclock.budzik.alarm.RecognitionHint
import pl.shotclock.budzik.alarm.RingController
import pl.shotclock.budzik.alarm.decideDismiss
import pl.shotclock.budzik.alarm.isPhotoAccepted
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.ml.ModelLabels
import pl.shotclock.budzik.ml.ObjectClassifier
import pl.shotclock.budzik.ml.confidenceOf

// Tyle widać komunikat o nieudanym zdjęciu, zanim wróci podpowiedź z analizy na żywo.
private const val PhotoRejectedMillis = 2_500L

/** Wszystko, co pokazuje ekran kamery. */
data class CameraUiState(
    val objectToFind: RequiredObject,
    // Pewność modelu dla szukanego przedmiotu, od 0 do 1.
    val confidence: Float = 0f,
    val hint: RecognitionHint = RecognitionHint.NotVisible,
    // false, dopóki nie przyjdzie pierwsza klatka (kamera się uruchamia).
    val hasFrame: Boolean = false,
    val isTakingPhoto: Boolean = false,
    val photoRejected: Boolean = false,
    // Nieudane ręczne zdjęcia. W Etapie 5 po 10 próbach pojawi się wyjście awaryjne.
    val failedAttempts: Int = 0,
) {
    val isRecognized: Boolean get() = hint == RecognitionHint.Recognized
}

/**
 * Rozpoznawanie przedmiotu na ekranie kamery. Dostaje klatki i zdjęcia, pyta klasyfikator,
 * a o wyłączeniu alarmu decyduje czysta funkcja [decideDismiss].
 *
 * Gdy nic nie dzwoni (podgląd z ustawień), szukamy pierwszego przedmiotu i nic nie wyłączamy.
 */
class CameraViewModel(
    private val ringController: RingController,
    createClassifier: (RequiredObject) -> ObjectClassifier,
) : ViewModel() {
    private val ringingAlarm = ringController.ringing.value
    private val objectToFind = ringingAlarm?.objectToFind ?: RequiredObject.FIRST
    private val requiredLabel = objectToFind.modelLabel ?: ModelLabels.FIRST
    // Drugi przedmiot, żeby podpowiedzieć „To inny przedmiot”.
    private val otherLabel = if (requiredLabel == ModelLabels.FIRST) ModelLabels.SECOND else ModelLabels.FIRST
    private val classifier = createClassifier(objectToFind)

    private val _uiState = MutableStateFlow(CameraUiState(objectToFind))
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    // Od kiedy pewność jest nad progiem. Zmienia go tylko analiza klatek (jeden wątek w tle).
    private var matchSinceMillis: Long? = null
    private var photoMessageJob: Job? = null

    /** Ekran kamery się otworzył: liczenie czasu utrzymania zaczyna się od zera. */
    fun onCameraOpened() {
        if (_uiState.value.isRecognized) return
        matchSinceMillis = null
        _uiState.update { it.copy(hasFrame = false, confidence = 0f, hint = RecognitionHint.NotVisible) }
    }

    /** Klatka z analizy na żywo. Wywoływana w wątku w tle co ok. 400 ms. */
    fun onFrame(image: Bitmap, timeMillis: Long) {
        if (_uiState.value.isRecognized) return
        val results = classifier.classify(image)
        val decision = decideDismiss(
            requiredConfidence = results.confidenceOf(requiredLabel),
            otherObjectConfidence = results.confidenceOf(otherLabel),
            nowMillis = timeMillis,
            matchSinceMillis = matchSinceMillis,
        )
        matchSinceMillis = decision.matchSinceMillis
        _uiState.update {
            it.copy(hasFrame = true, confidence = results.confidenceOf(requiredLabel), hint = decision.hint)
        }
        if (decision.shouldDismiss) onRecognized()
    }

    fun onTakePhotoStarted() {
        _uiState.update { it.copy(isTakingPhoto = true) }
    }

    /** Ręczne zdjęcie: wystarczy, że jedno zdjęcie przekroczy próg. Wywoływane w wątku w tle. */
    fun onPhotoTaken(image: Bitmap) {
        if (_uiState.value.isRecognized) return
        val confidence = classifier.classify(image).confidenceOf(requiredLabel)
        if (isPhotoAccepted(confidence)) {
            _uiState.update {
                it.copy(isTakingPhoto = false, confidence = confidence, hint = RecognitionHint.Recognized)
            }
            onRecognized()
        } else {
            _uiState.update {
                it.copy(isTakingPhoto = false, photoRejected = true, failedAttempts = it.failedAttempts + 1)
            }
            hidePhotoMessageLater()
        }
    }

    fun onPhotoFailed() {
        _uiState.update { it.copy(isTakingPhoto = false) }
    }

    private fun onRecognized() {
        // Wyłączamy tylko alarm, dla którego otwarto kamerę. Podgląd z ustawień niczego nie wyłącza.
        if (ringingAlarm != null) ringController.dismiss()
    }

    private fun hidePhotoMessageLater() {
        photoMessageJob?.cancel()
        photoMessageJob = viewModelScope.launch(Dispatchers.Default) {
            delay(PhotoRejectedMillis)
            _uiState.update { it.copy(photoRejected = false) }
        }
    }
}
