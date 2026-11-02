package pl.shotclock.budzik.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    background = GradientNavy,
    onBackground = TextPrimary,
    surface = GradientNavy,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
)

@Composable
fun ShotClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography) {
        // Nie używamy Surface z Material 3, więc domyślny kolor tekstu i ikon ustawiamy sami.
        CompositionLocalProvider(LocalContentColor provides TextPrimary, content = content)
    }
}
