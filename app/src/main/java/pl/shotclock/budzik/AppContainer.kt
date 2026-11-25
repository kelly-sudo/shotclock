package pl.shotclock.budzik

import android.content.Context
import pl.shotclock.budzik.data.AlarmRepository
import pl.shotclock.budzik.data.AppDatabase

/**
 * Jedno miejsce, w którym tworzymy obiekty potrzebne w całej aplikacji
 * (ręczne wstrzykiwanie zależności, bez Hilta). Każdy obiekt powstaje tu tylko raz.
 */
class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)

    val alarmRepository = AlarmRepository(database.alarmDao())
}
