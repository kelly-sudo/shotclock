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
}
