package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.TimeTextHuge
import pl.shotclock.budzik.ui.theme.TimeTextLarge

/** Large: lista alarmów. Huge: ekran dzwonienia. */
enum class TimeDisplaySize { Large, Huge }

@Composable
fun TimeDisplay(
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    size: TimeDisplaySize = TimeDisplaySize.Large,
) {
    Text(
        text = String.format(Locale.ROOT, "%02d:%02d", hour, minute),
        modifier = modifier,
        style = when (size) {
            TimeDisplaySize.Large -> TimeTextLarge
            TimeDisplaySize.Huge -> TimeTextHuge
        },
    )
}

@Preview(widthDp = 360, heightDp = 220)
@Composable
private fun TimeDisplayPreview() {
    ShotClockTheme {
        GlassBackground {
            Column {
                TimeDisplay(hour = 7, minute = 30)
                TimeDisplay(hour = 7, minute = 30, size = TimeDisplaySize.Huge)
            }
        }
    }
}
