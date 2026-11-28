package pl.shotclock.budzik

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import pl.shotclock.budzik.alarm.AlarmScheduler
import pl.shotclock.budzik.alarm.AndroidAlarmScheduler
import pl.shotclock.budzik.alarm.RingController
import pl.shotclock.budzik.data.AlarmRepository
import pl.shotclock.budzik.data.AppDatabase
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.data.SettingsRepository
import pl.shotclock.budzik.data.WakeEventRepository
import pl.shotclock.budzik.ml.FakeObjectClassifier
import pl.shotclock.budzik.ml.ModelInfo
import pl.shotclock.budzik.ml.ModelLabels
import pl.shotclock.budzik.ml.ObjectClassifier

/**
 * Jedno miejsce, w którym tworzymy obiekty potrzebne w całej aplikacji
 * (ręczne wstrzykiwanie zależności, bez Hilta). Każdy obiekt powstaje tu tylko raz.
 */
class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)
    private val alarmScheduler: AlarmScheduler = AndroidAlarmScheduler(context)

    /** Do pracy w tle, która ma trwać tak długo jak aplikacja, a nie jeden ekran. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val alarmRepository = AlarmRepository(database.alarmDao(), alarmScheduler)
    val wakeEventRepository = WakeEventRepository(database.wakeEventDao())
    val ringController = RingController(context)
    val settingsRepository = SettingsRepository.create(context)

    /** Czy zespół AI dostarczył już model (pliki w assets) i jaka to wersja. */
    val modelInfo = ModelInfo.read(context)

    /** Wersja aplikacji z build.gradle.kts (versionName), pokazywana w ustawieniach. */
    val appVersion: String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()

    /**
     * Klasyfikator do rozpoznawania przedmiotu. Na razie udawany: „widzi” przedmiot, którego szukamy.
     * W Etapie 8 zwróci tu TfliteObjectClassifier z prawdziwym modelem, gdy classifierMode(...) da ClassifierMode.Real
     * (model jest w assets, a tryb testowy w ustawieniach jest wyłączony). Wtedy [objectToFind] nie będzie potrzebny.
     */
    fun createClassifier(objectToFind: RequiredObject): ObjectClassifier =
        FakeObjectClassifier(pretendToSee = objectToFind.modelLabel ?: ModelLabels.FIRST)
}
