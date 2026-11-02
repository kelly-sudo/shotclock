package pl.shotclock.budzik.ui.history

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.objects.ObjectImage
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ChartBarColor
import pl.shotclock.budzik.ui.theme.PeachTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.StatNumberText
import pl.shotclock.budzik.ui.theme.SurfaceBorder
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.ui.theme.rememberPulse

private const val FlameMillis = 1200
private const val FlameGrowth = 0.08f

// Przykładowe czasy wstania w sekundach, od wtorku do dzisiejszego poniedziałku. Prawdziwe dane dojdą w Etapie 6.
private val SampleWakeSeconds = listOf(47, 140, 60, 180, 110, 75, 102)
private val SampleDayOrder = listOf(1, 2, 3, 4, 5, 6, 0)

/** Historia na przykładowych danych. Prawdziwe pobudki i statystyki dojdą w Etapie 6. */
@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    ScreenContainer(
        title = stringResource(R.string.tab_history),
        modifier = modifier,
        hasBottomBar = true,
    ) {
        StreakCard(Modifier.entranceFrom(rememberEntrance(order = 0)))
        WakeChartCard(Modifier.entranceFrom(rememberEntrance(order = 1)))
        WakeEventsCard(Modifier.entranceFrom(rememberEntrance(order = 2)))
    }
}

@Composable
private fun StreakCard(modifier: Modifier = Modifier) {
    val flame = rememberPulse(FlameMillis, label = "flame")

    AppCard(
        modifier = modifier.fillMaxWidth(),
        tint = PeachTint,
        shape = AppShapes.heroCard,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Image(
                painter = painterResource(R.drawable.ill_flame),
                contentDescription = null,
                modifier = Modifier
                    .size(Sizes.illustrationStreak)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 0.9f)
                        scaleX = 1f + FlameGrowth * flame.value
                        scaleY = 1f + FlameGrowth * flame.value
                    },
            )
            Column(Modifier.weight(1f)) {
                Text(text = stringResource(R.string.history_streak_sample), style = StatNumberText)
                Text(
                    text = stringResource(R.string.history_streak_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeachTint.secondaryText,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                StreakStat(R.string.history_best_sample, R.string.history_best_label)
                StreakStat(R.string.history_average_sample, R.string.history_average_label)
            }
        }
    }
}

@Composable
private fun StreakStat(@StringRes value: Int, @StringRes label: Int) {
    Column(horizontalAlignment = Alignment.End) {
        Text(text = stringResource(value), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodySmall,
            color = PeachTint.secondaryText,
        )
    }
}

@Composable
private fun WakeChartCard(modifier: Modifier = Modifier) {
    val dayNames = stringArrayResource(R.array.weekdays_short)
    val longest = SampleWakeSeconds.max()

    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.history_chart_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SampleWakeSeconds.forEachIndexed { index, seconds ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Box(
                        modifier = Modifier.height(Sizes.chartHeight),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        ChartBar(
                            heightFraction = seconds.toFloat() / longest,
                            isToday = index == SampleWakeSeconds.lastIndex,
                            order = index,
                        )
                    }
                    Text(
                        text = dayNames[SampleDayOrder[index]],
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartBar(heightFraction: Float, isToday: Boolean, order: Int) {
    val grow = rememberEntrance(order)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Sizes.chartHeight * heightFraction)
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 1f)
                scaleY = grow.value
            }
            .clip(AppShapes.chartBar)
            .background(if (isToday) Accent else ChartBarColor),
    )
}

@Composable
private fun WakeEventsCard(modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        WakeEventRow(
            visual = ObjectVisual.Toothbrush,
            title = R.string.history_event_today,
            details = R.string.history_event_today_details,
            time = R.string.history_event_today_time,
        )
        HorizontalDivider(color = SurfaceBorder)
        WakeEventRow(
            visual = ObjectVisual.Mug,
            title = R.string.history_event_yesterday,
            details = R.string.history_event_yesterday_details,
            time = R.string.history_event_yesterday_time,
        )
    }
}

@Composable
private fun WakeEventRow(
    visual: ObjectVisual,
    @StringRes title: Int,
    @StringRes details: Int,
    @StringRes time: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.illustrationHistoryBox)
                .clip(AppShapes.thumbnail)
                .background(visual.tint.surface),
            contentAlignment = Alignment.Center,
        ) {
            ObjectImage(visual, contentDescription = null, modifier = Modifier.size(Sizes.illustrationSmall))
        }
        Column(Modifier.weight(1f)) {
            Text(text = stringResource(title), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(details),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        Text(text = stringResource(time), style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun HistoryScreenPreview() {
    ShotClockTheme {
        AppBackground {
            HistoryScreen()
        }
    }
}
