package pl.shotclock.budzik.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.max
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import pl.shotclock.budzik.R
import pl.shotclock.budzik.data.DayWakeTime
import pl.shotclock.budzik.data.HistorySummary
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.data.WakeEvent
import pl.shotclock.budzik.data.summarizeHistory
import pl.shotclock.budzik.data.wakeUpSeconds
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.components.formatDuration
import pl.shotclock.budzik.ui.objects.ObjectImage
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ChartBarColor
import pl.shotclock.budzik.ui.theme.ControlOffColor
import pl.shotclock.budzik.ui.theme.NeutralTint
import pl.shotclock.budzik.ui.theme.PeachTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.SmallLabelMinSize
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.StatNumberText
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.ui.theme.rememberPulse
import pl.shotclock.budzik.viewmodel.AppViewModelProvider
import pl.shotclock.budzik.viewmodel.HistoryUiState
import pl.shotclock.budzik.viewmodel.HistoryViewModel

private const val FlameMillis = 1200
private const val FlameGrowth = 0.08f

// Godzina i data na liście pobudek, np. „7:31” i „12 lis”. Teksty aplikacji są po polsku, więc i nazwy miesięcy.
private val TimeFormat = DateTimeFormatter.ofPattern("H:mm")
private val DateFormat = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("pl"))

/** Ekran historii z ViewModelem. */
@Composable
fun HistoryRoute(viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(uiState)
}

/** Wygląd historii. Dostaje gotowy stan, więc działa też w @Preview, bez bazy. */
@Composable
fun HistoryScreen(uiState: HistoryUiState, modifier: Modifier = Modifier) {
    ScreenContainer(
        title = stringResource(R.string.tab_history),
        modifier = modifier,
        hasBottomBar = true,
    ) {
        val summary = uiState.summary
        when {
            // Zanim baza odpowie, pokazujemy tylko tytuł.
            uiState.isLoading || summary == null -> Unit
            uiState.isEmpty -> EmptyHistoryCard(Modifier.entranceFrom(rememberEntrance(order = 0)))
            else -> {
                StreakCard(summary, Modifier.entranceFrom(rememberEntrance(order = 0)))
                WakeChartCard(summary.lastSevenDays, Modifier.entranceFrom(rememberEntrance(order = 1)))
                WakeEventsCard(
                    events = uiState.recentEvents,
                    today = uiState.today,
                    zone = uiState.zone,
                    modifier = Modifier.entranceFrom(rememberEntrance(order = 2)),
                )
            }
        }
    }
}

@Composable
private fun FlameImage() {
    val flame = rememberPulse(FlameMillis, label = "flame")
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
}

@Composable
private fun StreakCard(summary: HistorySummary, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        tint = PeachTint,
        shape = AppShapes.heroCard,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            FlameImage()
            Column(Modifier.weight(1f)) {
                Text(text = summary.streakDays.toString(), style = StatNumberText)
                Text(
                    text = pluralStringResource(R.plurals.history_streak_label, summary.streakDays),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeachTint.secondaryText,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                StreakStat(summary.bestStreakDays.toString(), stringResource(R.string.history_best_label))
                StreakStat(
                    value = summary.averageWakeUpSeconds?.let { formatDuration(it) }
                        ?: stringResource(R.string.history_no_value),
                    label = stringResource(R.string.history_average_label),
                )
            }
        }
    }
}

@Composable
private fun StreakStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = PeachTint.secondaryText,
        )
    }
}

/** Pusty stan: jeszcze żadnej pobudki w bazie. */
@Composable
private fun EmptyHistoryCard(modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        tint = PeachTint,
        shape = AppShapes.heroCard,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
        ) {
            FlameImage()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                Text(text = stringResource(R.string.history_empty_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.history_empty_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeachTint.secondaryText,
                )
            }
        }
    }
}

@Composable
private fun WakeChartCard(days: List<DayWakeTime>, modifier: Modifier = Modifier) {
    // Obie tablice zaczynają się od poniedziałku, tak jak DayOfWeek (poniedziałek ma wartość 1).
    val dayNames = stringArrayResource(R.array.weekdays_short)
    val fullDayNames = stringArrayResource(R.array.weekdays_full)
    // Najdłuższy czas to pełna wysokość słupka. coerceAtLeast chroni przed dzieleniem przez zero.
    val longest = (days.mapNotNull { it.seconds }.maxOrNull() ?: 1).coerceAtLeast(1)

    // Czytnik ekranu nie „widzi” słupków, więc dostaje cały wykres jako jedno zdanie z wartościami.
    val chartDescription = stringResource(
        R.string.history_chart_description,
        days.map { day ->
            val name = fullDayNames[day.date.dayOfWeek.value - 1]
            if (day.seconds == null) {
                stringResource(R.string.history_chart_day_empty, name)
            } else {
                stringResource(R.string.history_chart_day, name, formatDuration(day.seconds))
            }
        }.joinToString(", "),
    )

    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.history_chart_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier.clearAndSetSemantics { contentDescription = chartDescription },
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            days.forEachIndexed { index, day ->
                val isToday = index == days.lastIndex
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Column(
                        modifier = Modifier.height(Sizes.chartHeight + Sizes.chartValueSpace),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall, Alignment.Bottom),
                    ) {
                        // Dzisiejszy wynik jest podpisany liczbą, żeby nie trzeba było zgadywać z wysokości słupka.
                        if (isToday && day.seconds != null) {
                            val valueStyle = MaterialTheme.typography.labelMedium
                            Text(
                                text = stringResource(R.string.history_chart_value, day.seconds / 60, day.seconds % 60),
                                modifier = Modifier.entranceFrom(rememberEntrance(index)),
                                style = valueStyle,
                                color = Accent,
                                maxLines = 1,
                                // Kolumna słupka jest wąska, więc przy dużej czcionce liczba zmniejsza się zamiast uciąć.
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = SmallLabelMinSize,
                                    maxFontSize = valueStyle.fontSize,
                                ),
                            )
                        }
                        ChartBar(
                            heightFraction = (day.seconds ?: 0).toFloat() / longest,
                            isToday = isToday,
                            isEmpty = day.seconds == null,
                            order = index,
                        )
                    }
                    // Dziś wyróżnia nie tylko kolor słupka, ale też napis „Dziś” w jasnym kolorze.
                    Text(
                        text = if (isToday) {
                            stringResource(R.string.history_chart_today)
                        } else {
                            dayNames[day.date.dayOfWeek.value - 1]
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) TextPrimary else TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartBar(heightFraction: Float, isToday: Boolean, isEmpty: Boolean, order: Int) {
    val grow = rememberEntrance(order)
    val color = when {
        // Dzień bez pobudki to tylko niska, ciemniejsza kreska.
        isEmpty -> ControlOffColor
        isToday -> Accent
        else -> ChartBarColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(max(Sizes.chartHeight * heightFraction, Sizes.chartBarMin))
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 1f)
                scaleY = grow.value
            }
            .clip(AppShapes.chartBar)
            .background(color),
    )
}

@Composable
private fun WakeEventsCard(
    events: List<WakeEvent>,
    today: LocalDate,
    zone: ZoneId,
    modifier: Modifier = Modifier,
) {
    // Rzędy mają stałą wysokość, więc karta ma mały odstęp w pionie i nie dodaje przerw między rzędami.
    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.screenEdge, vertical = Spacing.extraSmall),
        verticalSpacing = Spacing.none,
    ) {
        Text(
            text = stringResource(R.string.history_events_title),
            modifier = Modifier
                .padding(top = Spacing.medium, bottom = Spacing.extraSmall)
                .semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
        )
        events.forEachIndexed { index, event ->
            if (index > 0) HorizontalDivider(color = NeutralTint.border)
            WakeEventRow(event, today, zone)
        }
    }
}

@Composable
private fun WakeEventRow(event: WakeEvent, today: LocalDate, zone: ZoneId) {
    // Przy wyłączeniu awaryjnym nie wiemy, jaki przedmiot był w kadrze, więc zamiast ilustracji jest ikona budzika.
    val visual = event.recognizedObject?.let { ObjectVisual.of(it) }

    Row(
        modifier = Modifier.heightIn(min = Sizes.listRowHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.illustrationHistoryBox)
                .clip(AppShapes.thumbnailSmall)
                .background(visual?.tint?.surface ?: ControlOffColor),
            contentAlignment = Alignment.Center,
        ) {
            if (visual != null) {
                ObjectImage(visual, contentDescription = null, modifier = Modifier.size(Sizes.illustrationSmall))
            } else {
                Icon(painterResource(R.drawable.ic_alarm), contentDescription = null, tint = TextSecondary)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(text = eventWhen(event, today, zone), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = eventDetails(event, visual),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        Text(text = formatDuration(wakeUpSeconds(event)), style = MaterialTheme.typography.titleMedium)
    }
}

/** Kiedy wyłączono alarm: „Dziś, 7:31”, „Wczoraj, 9:01” albo „12 lis, 7:30”. */
@Composable
private fun eventWhen(event: WakeEvent, today: LocalDate, zone: ZoneId): String {
    val dismissed = Instant.ofEpochMilli(event.dismissedAt).atZone(zone)
    val time = dismissed.format(TimeFormat)
    return when (dismissed.toLocalDate()) {
        today -> stringResource(R.string.history_event_today, time)
        today.minusDays(1) -> stringResource(R.string.history_event_yesterday, time)
        else -> stringResource(R.string.history_event_date, dismissed.format(DateFormat), time)
    }
}

/** Szczegóły pobudki: „Szczoteczka · 96% · 1 próba” albo „Wyłączenie awaryjne · 3 próby”. */
@Composable
private fun eventDetails(event: WakeEvent, visual: ObjectVisual?): String {
    val attempts = pluralStringResource(R.plurals.history_attempts, event.attempts, event.attempts)
    if (event.emergency || visual == null) return stringResource(R.string.history_event_emergency, attempts)
    val percent = ((event.confidence ?: 0f) * 100).roundToInt()
    return stringResource(R.string.history_event_details, stringResource(visual.shortNameRes), percent, attempts)
}

/** Przykładowe pobudki do podglądów: dziś, wczoraj i kilka dni wstecz, jedna awaryjna. */
private fun samplePreviewState(): HistoryUiState {
    val zone = ZoneId.of("Europe/Warsaw")
    val today = LocalDate.of(2026, 11, 30)
    fun wake(daysAgo: Long, hour: Int, minute: Int, seconds: Int, item: RequiredObject?, attempts: Int = 1): WakeEvent {
        val dismissed = LocalDateTime.of(today.minusDays(daysAgo), LocalTime.of(hour, minute))
            .atZone(zone).toInstant().toEpochMilli()
        return WakeEvent(
            alarmId = 1,
            ringStartedAt = dismissed - seconds * 1000L,
            dismissedAt = dismissed,
            attempts = attempts,
            recognizedObject = item,
            confidence = item?.let { 0.93f },
            emergency = item == null,
        )
    }
    val events = listOf(
        wake(0, 7, 31, 102, RequiredObject.FIRST),
        wake(1, 9, 1, 75, RequiredObject.SECOND, attempts = 2),
        wake(2, 7, 30, 110, RequiredObject.FIRST),
        wake(3, 7, 30, 640, null, attempts = 10),
        wake(4, 7, 30, 60, RequiredObject.FIRST),
        wake(6, 7, 30, 47, RequiredObject.SECOND),
    )
    return HistoryUiState(
        isLoading = false,
        summary = summarizeHistory(events, today, zone),
        recentEvents = events,
        today = today,
        zone = zone,
    )
}

@Preview(widthDp = 390, heightDp = 1100)
@Composable
private fun HistoryScreenPreview() {
    ShotClockTheme {
        AppBackground {
            HistoryScreen(samplePreviewState())
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun HistoryScreenEmptyPreview() {
    ShotClockTheme {
        AppBackground {
            val summary = summarizeHistory(emptyList(), LocalDate.now(), ZoneId.systemDefault())
            HistoryScreen(HistoryUiState(isLoading = false, summary = summary))
        }
    }
}
