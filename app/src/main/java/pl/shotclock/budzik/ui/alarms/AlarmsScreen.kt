package pl.shotclock.budzik.ui.alarms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.GlassBackground
import pl.shotclock.budzik.ui.components.GlassButtonStyle
import pl.shotclock.budzik.ui.components.GlassCard
import pl.shotclock.budzik.ui.components.GlassIconButton
import pl.shotclock.budzik.ui.components.GlassToggle
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.components.TimeDisplay
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary

/** Zaślepka listy alarmów. W Etapie 2 dane przyjdą z bazy przez ViewModel. */
@Composable
fun AlarmsScreen(
    onAddAlarm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        ScreenContainer(title = stringResource(R.string.tab_alarms), hasBottomBar = true) {
            Text(
                text = stringResource(R.string.alarms_next_sample),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )
            SampleAlarmCard()
            Text(
                text = stringResource(R.string.alarms_sample_note),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        GlassIconButton(
            icon = R.drawable.ic_add,
            contentDescription = stringResource(R.string.action_add_alarm),
            onClick = onAddAlarm,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = Spacing.large, bottom = Sizes.bottomBarClearance),
            style = GlassButtonStyle.Primary,
        )
    }
}

@Composable
private fun SampleAlarmCard() {
    var enabled by rememberSaveable { mutableStateOf(true) }

    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                TimeDisplay(hour = 7, minute = 30)
                Text(
                    text = stringResource(R.string.alarms_sample_days),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_category),
                        contentDescription = null,
                        tint = TextSecondary,
                    )
                    Text(
                        text = stringResource(R.string.alarms_sample_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
            GlassToggle(
                checked = enabled,
                onCheckedChange = { enabled = it },
                contentDescription = stringResource(R.string.alarms_sample_toggle),
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun AlarmsScreenPreview() {
    ShotClockTheme {
        GlassBackground {
            AlarmsScreen(onAddAlarm = {})
        }
    }
}
