package pl.shotclock.budzik

import android.app.Application
import kotlinx.coroutines.launch
import pl.shotclock.budzik.alarm.RingNotification

/** Startuje jako pierwsza, przed ekranami, i przechowuje AppContainer przez cały czas działania aplikacji. */
class ShotClockApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        RingNotification.createChannel(this)
        // Po „Wymuś zatrzymanie” system kasuje alarmy aplikacji. Przy każdym starcie ustawiamy je więc jeszcze raz.
        container.applicationScope.launch { container.alarmRepository.rescheduleAll() }
    }
}
