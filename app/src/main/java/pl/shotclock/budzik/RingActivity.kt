package pl.shotclock.budzik

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.ring.RingRoute
import pl.shotclock.budzik.ui.theme.ShotClockTheme

/**
 * Osobne okno ekranu dzwonienia. Otwiera je powiadomienie alarmu, także na zablokowanym telefonie.
 * Jest oddzielone od MainActivity, żeby po wyłączeniu alarmu użytkownik nie lądował w środku aplikacji.
 */
class RingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            ShotClockTheme {
                AppBackground {
                    RingRoute(onFinished = ::finish)
                }
            }
        }
    }

    /** Pokazuje okno nad ekranem blokady, włącza ekran i nie pozwala mu zgasnąć, dopóki alarm dzwoni. */
    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            // Android 8.0 (API 26) nie ma jeszcze tych funkcji, więc używamy starszych flag okna.
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
