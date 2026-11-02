package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.TimeTextHuge
import pl.shotclock.budzik.ui.theme.TimeTextLarge
import pl.shotclock.budzik.ui.theme.TimeTextMedium
import pl.shotclock.budzik.ui.theme.TimeTextMinSize

/** Medium: zwykła karta alarmu. Large: główna karta i edycja. Huge: ekran dzwonienia. */
enum class TimeDisplaySize { Medium, Large, Huge }

@Composable
fun TimeDisplay(
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    size: TimeDisplaySize = TimeDisplaySize.Large,
) {
    val style = when (size) {
        TimeDisplaySize.Medium -> TimeTextMedium
        TimeDisplaySize.Large -> TimeTextLarge
        TimeDisplaySize.Huge -> TimeTextHuge
    }

    // Godzina nigdy nie może się złamać na dwa wiersze, więc gdy brakuje miejsca, czcionka się zmniejsza.
    Text(
        text = String.format(Locale.ROOT, "%02d:%02d", hour, minute),
        modifier = modifier,
        style = style,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(minFontSize = TimeTextMinSize, maxFontSize = style.fontSize),
    )
}

@Preview(widthDp = 360, heightDp = 260)
@Composable
private fun TimeDisplayPreview() {
    ShotClockTheme {
        AppBackground {
            Column {
                TimeDisplay(hour = 7, minute = 30, size = TimeDisplaySize.Medium)
                TimeDisplay(hour = 7, minute = 30)
                TimeDisplay(hour = 7, minute = 30, size = TimeDisplaySize.Huge)
            }
        }
    }
}
