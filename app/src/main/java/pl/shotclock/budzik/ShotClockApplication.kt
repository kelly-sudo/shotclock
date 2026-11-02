package pl.shotclock.budzik

import android.app.Application

/** Startuje jako pierwsza, przed ekranami, i przechowuje AppContainer przez cały czas działania aplikacji. */
class ShotClockApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
