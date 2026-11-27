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
import pl.shotclock.budzik.ml.FakeObjectClassifier
import pl.shotclock.budzik.ml.ModelLabels
import pl.shotclock.budzik.ml.ObjectClassifier
import pl.shotclock.budzik.ml.TfliteObjectClassifier

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
    val ringController = RingController(context)

    // Prawdziwy model wczytujemy raz, przy pierwszym otwarciu kamery (to kilka MB, szkoda czytać je za każdym razem).
    private val realClassifier: ObjectClassifier? by lazy {
        TfliteObjectClassifier.createIfAvailable(context.applicationContext)
    }

    /**
     * Klasyfikator do rozpoznawania przedmiotu: prawdziwy model, jeśli jest w assets,
     * a jeśli go nie ma, udawany, który „widzi” przedmiot, którego szukamy.
     */
    fun createClassifier(objectToFind: RequiredObject): ObjectClassifier =
        realClassifier ?: FakeObjectClassifier(pretendToSee = objectToFind.modelLabel ?: ModelLabels.FIRST)
}
