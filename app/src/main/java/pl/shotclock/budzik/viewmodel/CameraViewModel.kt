package pl.shotclock.budzik.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.shotclock.budzik.alarm.DefaultConfidenceThreshold
import pl.shotclock.budzik.alarm.RecognitionHint
import pl.shotclock.budzik.alarm.RingController
import pl.shotclock.budzik.alarm.decideDismiss
import pl.shotclock.budzik.alarm.isPhotoAccepted
import pl.shotclock.budzik.data.AppSettings
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.ml.ClassificationResult
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
    // Próg z ustawień. Ekran rysuje go jako kreskę na pasku pewności.
    val threshold: Float = DefaultConfidenceThreshold,
    // Wyniki modelu dla wszystkich klas z ostatniej klatki (podgląd na gałęzi testowej).
    val modelScores: List<ClassificationResult> = emptyList(),
) {
    val isRecognized: Boolean get() = hint == RecognitionHint.Recognized
}

/**
 * Rozpoznawanie przedmiotu na ekranie kamery. Dostaje klatki i zdjęcia, pyta klasyfikator,
 * a o tym, czy przedmiot rozpoznano, decyduje czysta funkcja [decideDismiss].
 *
 * Sam alarm wyłącza RingViewModel: ekran kamery zgłasza mu rozpoznanie (CameraRoute, onRecognized).
 * Gdy nic nie dzwoni (podgląd z ustawień), szukamy pierwszego przedmiotu i nic nie wyłączamy.
 */
class CameraViewModel(
    private val ringController: RingController,
    createClassifier: (RequiredObject) -> ObjectClassifier,
    settings: Flow<AppSettings>,
) : ViewModel() {
    private val objectToFind = ringController.ringing.value?.objectToFind ?: RequiredObject.FIRST
    private val requiredLabel = objectToFind.modelLabel ?: ModelLabels.FIRST
    // Drugi przedmiot, żeby podpowiedzieć „To inny przedmiot”.
    private val otherLabel = if (requiredLabel == ModelLabels.FIRST) ModelLabels.SECOND else ModelLabels.FIRST
    private val classifier = createClassifier(objectToFind)

    private val _uiState = MutableStateFlow(CameraUiState(objectToFind))
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    // Od kiedy pewność jest nad progiem. Zmienia go tylko analiza klatek (jeden wątek w tle).
    private var matchSinceMillis: Long? = null
    private var photoMessageJob: Job? = null

    // Próg pewności z ustawień deweloperskich. Zanim ustawienia się wczytają, obowiązuje domyślne 55%.
    @Volatile
    private var threshold = DefaultConfidenceThreshold

    init {
        viewModelScope.launch {
            settings.collect { settings ->
                threshold = settings.confidenceThreshold
                _uiState.update { it.copy(threshold = settings.confidenceThreshold) }
            }
        }
    }

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
            threshold = threshold,
        )
        matchSinceMillis = decision.matchSinceMillis
        _uiState.update {
            it.copy(
                hasFrame = true,
                confidence = results.confidenceOf(requiredLabel),
                hint = decision.hint,
                modelScores = results,
            )
        }
    }

    fun onTakePhotoStarted() {
        _uiState.update { it.copy(isTakingPhoto = true) }
    }

    /** Ręczne zdjęcie: wystarczy, że jedno zdjęcie przekroczy próg. Wywoływane w wątku w tle. */
    fun onPhotoTaken(image: Bitmap) {
        if (_uiState.value.isRecognized) return
        val confidence = classifier.classify(image).confidenceOf(requiredLabel)
        if (isPhotoAccepted(confidence, threshold)) {
            _uiState.update {
                it.copy(isTakingPhoto = false, confidence = confidence, hint = RecognitionHint.Recognized)
            }
        } else {
            _uiState.update { it.copy(isTakingPhoto = false, photoRejected = true) }
            // Licznik nieudanych prób trzyma RingController, bo z niego korzysta też ekran dzwonienia.
            ringController.onPhotoRejected()
            hidePhotoMessageLater()
        }
    }

    fun onPhotoFailed() {
        _uiState.update { it.copy(isTakingPhoto = false) }
    }

    private fun hidePhotoMessageLater() {
        photoMessageJob?.cancel()
        photoMessageJob = viewModelScope.launch(Dispatchers.Default) {
            delay(PhotoRejectedMillis)
            _uiState.update { it.copy(photoRejected = false) }
        }
    }
}
