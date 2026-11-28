package pl.shotclock.budzik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.shotclock.budzik.data.Alarm
import pl.shotclock.budzik.data.AppSettings
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.ml.ClassifierMode
import pl.shotclock.budzik.ml.ModelInfo
import pl.shotclock.budzik.ml.classifierMode

private const val StopTimeoutMillis = 5_000L

/** Wszystko, co pokazuje ekran przedmiotów. */
data class ObjectsUiState(
    // Ile alarmów wymaga danego przedmiotu. Alarmy z losowym przedmiotem są tu policzone osobno.
    val alarmCounts: Map<RequiredObject, Int> = emptyMap(),
    val classifierMode: ClassifierMode = ClassifierMode.Test,
    val modelVersion: String = "",
)

/** Liczy alarmy dla każdego przedmiotu. Czysta funkcja, żeby dało się ją sprawdzić testem. */
fun countAlarmsByObject(alarms: List<Alarm>): Map<RequiredObject, Int> =
    alarms.groupingBy { it.requiredObject }.eachCount()

class ObjectsViewModel(
    alarms: Flow<List<Alarm>>,
    settings: Flow<AppSettings>,
    modelInfo: ModelInfo,
) : ViewModel() {

    val uiState: StateFlow<ObjectsUiState> =
        combine(alarms, settings) { alarmList, appSettings ->
            ObjectsUiState(
                alarmCounts = countAlarmsByObject(alarmList),
                classifierMode = classifierMode(appSettings.testMode, modelInfo.isAvailable),
                modelVersion = modelInfo.version,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
            initialValue = ObjectsUiState(modelVersion = modelInfo.version),
        )
}
