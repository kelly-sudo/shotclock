package pl.shotclock.budzik

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.shotclock.budzik.ui.AppNavigation
import pl.shotclock.budzik.ui.theme.ShotClockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Aplikacja jest zawsze ciemna, więc ikony paska stanu mają być jasne także w jasnym trybie telefonu.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            ShotClockTheme {
                AppNavigation()
            }
        }
    }
}
