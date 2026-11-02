package pl.shotclock.budzik.ui.ring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.GlassBackground
import pl.shotclock.budzik.ui.components.GlassButton
import pl.shotclock.budzik.ui.components.GlassCard
import pl.shotclock.budzik.ui.components.TimeDisplay
import pl.shotclock.budzik.ui.components.TimeDisplaySize
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Spacing

/**
 * Zaślepka ekranu dzwonienia. Prawdziwy alarm dojdzie w Etapie 3.
 * Celowo nie ma tu przycisku drzemki.
 */
@Composable
fun RingScreen(
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassBackground(modifier = modifier, animated = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge, Alignment.CenterVertically),
        ) {
            TimeDisplay(hour = 7, minute = 30, size = TimeDisplaySize.Huge)
            GlassCard(Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_category),
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.ring_message_sample),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            GlassButton(
                text = stringResource(R.string.ring_open_camera),
                onClick = onOpenCamera,
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.ic_camera,
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun RingScreenPreview() {
    ShotClockTheme {
        RingScreen(onOpenCamera = {})
    }
}
