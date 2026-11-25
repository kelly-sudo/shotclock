package pl.shotclock.budzik.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    background = BackgroundColor,
    onBackground = TextPrimary,
    surface = SurfaceColor,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    // Pozostałe role też ustawiamy na nasze kolory. Bez tego komponenty Material 3
    // (np. Snackbar „Cofnij” w Etapie 2, wybór godziny, linie podziału) dostałyby domyślny fiolet.
    secondary = Accent,
    onSecondary = OnAccent,
    tertiary = Accent,
    onTertiary = OnAccent,
    surfaceVariant = SurfaceColor,
    surfaceContainerLowest = BackgroundColor,
    surfaceContainerLow = SurfaceColor,
    surfaceContainer = SurfaceColor,
    surfaceContainerHigh = SurfaceColor,
    surfaceContainerHighest = ControlOffColor,
    outline = ControlBorder,
    outlineVariant = SurfaceBorder,
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundColor,
    inversePrimary = OnAccent,
)

@Composable
fun ShotClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography) {
        // Nie używamy Surface z Material 3, więc domyślny kolor tekstu i ikon ustawiamy sami.
        CompositionLocalProvider(LocalContentColor provides TextPrimary, content = content)
    }
}
