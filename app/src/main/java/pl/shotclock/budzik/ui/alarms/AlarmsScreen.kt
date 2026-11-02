package pl.shotclock.budzik.ui.alarms

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButtonStyle
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.AppIconButton
import pl.shotclock.budzik.ui.components.AppToggle
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.components.TimeDisplay
import pl.shotclock.budzik.ui.components.TimeDisplaySize
import pl.shotclock.budzik.ui.objects.ObjectImage
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance

private const val DisabledCardAlpha = 0.6f

/** Lista alarmów na przykładowych danych. W Etapie 2 dane przyjdą z bazy przez ViewModel. */
@Composable
fun AlarmsScreen(
    onAddAlarm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        ScreenContainer(title = stringResource(R.string.tab_alarms), hasBottomBar = true) {
            NextAlarmPill(Modifier.entranceFrom(rememberEntrance(order = 0)))
            HeroAlarmCard(Modifier.entranceFrom(rememberEntrance(order = 1)))
            AlarmCard(
                hour = 9,
                minute = 0,
                daysRes = R.string.alarms_sample_days_weekend,
                visual = ObjectVisual.Mug,
                initiallyEnabled = false,
                modifier = Modifier.entranceFrom(rememberEntrance(order = 2)),
            )
            AlarmCard(
                hour = 6,
                minute = 15,
                daysRes = R.string.alarms_sample_days_gym,
                visual = ObjectVisual.Random,
                initiallyEnabled = true,
                modifier = Modifier.entranceFrom(rememberEntrance(order = 3)),
            )
            // Dzięki temu ostatnią kartę da się przewinąć ponad pływający przycisk dodawania.
            Spacer(Modifier.height(Sizes.floatingButton))
        }
        AppIconButton(
            icon = R.drawable.ic_add,
            contentDescription = stringResource(R.string.action_add_alarm),
            onClick = onAddAlarm,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = Spacing.screenEdge, bottom = Sizes.bottomBarClearance)
                .entranceFrom(rememberEntrance(order = 4)),
            style = AppButtonStyle.Primary,
            size = Sizes.floatingButton,
        )
    }
}

@Composable
private fun NextAlarmPill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(AppShapes.pill)
            .background(MintTint.surface)
            .padding(horizontal = Spacing.smallMedium, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Icon(painterResource(R.drawable.ic_alarm), contentDescription = null, tint = Accent)
        Text(
            text = stringResource(R.string.alarms_next_sample),
            style = MaterialTheme.typography.labelMedium,
            color = Accent,
        )
    }
}

@Composable
private fun HeroAlarmCard(modifier: Modifier = Modifier) {
    var enabled by rememberSaveable { mutableStateOf(true) }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        tint = MintTint,
        shape = AppShapes.heroCard,
        contentPadding = PaddingValues(
            start = Spacing.screenEdge,
            end = Spacing.screenEdge,
            top = Spacing.screenEdge,
            bottom = Spacing.extraSmall,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                TimeDisplay(hour = 7, minute = 30)
                Text(
                    text = stringResource(R.string.alarms_sample_days_work),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MintTint.secondaryText,
                )
            }
            // Na wąskim ekranie ilustracja się zmniejsza, żeby zostawić miejsce na godzinę.
            ObjectImage(
                visual = ObjectVisual.Toothbrush,
                contentDescription = stringResource(ObjectVisual.Toothbrush.nameRes),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .sizeIn(maxWidth = Sizes.illustrationHero, maxHeight = Sizes.illustrationHero)
                    .aspectRatio(1f),
            )
        }
        HorizontalDivider(color = MintTint.border)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.alarms_sample_task),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            AppToggle(
                checked = enabled,
                onCheckedChange = { enabled = it },
                contentDescription = stringResource(R.string.alarms_toggle),
            )
        }
    }
}

@Composable
private fun AlarmCard(
    hour: Int,
    minute: Int,
    @StringRes daysRes: Int,
    visual: ObjectVisual,
    initiallyEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var enabled by rememberSaveable { mutableStateOf(initiallyEnabled) }
    val contentAlpha by animateFloatAsState(if (enabled) 1f else DisabledCardAlpha, label = "alarm alpha")

    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Spacing.screenEdge,
            end = Spacing.smallMedium,
            top = Spacing.smallMedium,
            bottom = Spacing.smallMedium,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = contentAlpha },
            ) {
                TimeDisplay(hour = hour, minute = minute, size = TimeDisplaySize.Medium)
                Text(
                    text = stringResource(daysRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Box(
                modifier = Modifier
                    .size(Sizes.illustrationThumbnail)
                    .graphicsLayer { alpha = contentAlpha }
                    .clip(AppShapes.thumbnail)
                    .background(visual.tint.surface),
                contentAlignment = Alignment.Center,
            ) {
                ObjectImage(
                    visual = visual,
                    contentDescription = stringResource(visual.nameRes),
                    modifier = Modifier.size(Sizes.illustrationRow),
                )
            }
            AppToggle(
                checked = enabled,
                onCheckedChange = { enabled = it },
                contentDescription = stringResource(R.string.alarms_toggle),
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AlarmsScreenPreview() {
    ShotClockTheme {
        AppBackground {
            AlarmsScreen(onAddAlarm = {})
        }
    }
}
