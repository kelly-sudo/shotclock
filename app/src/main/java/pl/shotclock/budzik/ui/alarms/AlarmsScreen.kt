package pl.shotclock.budzik.ui.alarms

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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import pl.shotclock.budzik.R
import pl.shotclock.budzik.alarm.TimeUntil
import pl.shotclock.budzik.data.Alarm
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.data.Weekdays
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
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
import pl.shotclock.budzik.viewmodel.AlarmsUiState
import pl.shotclock.budzik.viewmodel.AlarmsViewModel
import pl.shotclock.budzik.viewmodel.AppViewModelProvider

private const val DisabledCardAlpha = 0.6f
// Przy długiej liście dalsze karty wjeżdżają razem, żeby nie czekać na nie zbyt długo.
private const val MaxEntranceOrder = 6

/** Ekran listy z ViewModelem: czyta stan i przekazuje akcje. Sam wygląd jest w [AlarmsScreen]. */
@Composable
fun AlarmsRoute(
    onAddAlarm: () -> Unit,
    onOpenAlarm: (Long) -> Unit,
    viewModel: AlarmsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlarmsScreen(
        uiState = uiState,
        onAddAlarm = onAddAlarm,
        onOpenAlarm = onOpenAlarm,
        onEnabledChange = viewModel::setEnabled,
    )
}

/** Wygląd listy alarmów. Dostaje gotowy stan, więc działa też w @Preview, bez bazy. */
@Composable
fun AlarmsScreen(
    uiState: AlarmsUiState,
    onAddAlarm: () -> Unit,
    onOpenAlarm: (Long) -> Unit,
    onEnabledChange: (Alarm, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasAlarms = !uiState.isLoading && !uiState.isEmpty

    Box(modifier.fillMaxSize()) {
        ScreenContainer(title = stringResource(R.string.tab_alarms), hasBottomBar = true) {
            if (uiState.isEmpty) {
                EmptyAlarms(onAddAlarm, Modifier.entranceFrom(rememberEntrance(order = 0)))
            }
            if (hasAlarms) {
                NextAlarmPill(uiState.timeUntilNext, Modifier.entranceFrom(rememberEntrance(order = 0)))
                uiState.nextAlarm?.let { alarm ->
                    // key sprawia, że stan karty (np. animacja) należy do konkretnego alarmu, a nie do miejsca na liście.
                    key(alarm.id) {
                        HeroAlarmCard(
                            alarm = alarm,
                            onClick = { onOpenAlarm(alarm.id) },
                            onEnabledChange = { onEnabledChange(alarm, it) },
                            modifier = Modifier.entranceFrom(rememberEntrance(order = 1)),
                        )
                    }
                }
                uiState.otherAlarms.forEachIndexed { index, alarm ->
                    key(alarm.id) {
                        AlarmCard(
                            alarm = alarm,
                            onClick = { onOpenAlarm(alarm.id) },
                            onEnabledChange = { onEnabledChange(alarm, it) },
                            modifier = Modifier.entranceFrom(
                                rememberEntrance(order = minOf(index + 2, MaxEntranceOrder)),
                            ),
                        )
                    }
                }
                // Dzięki temu ostatnią kartę da się przewinąć ponad pływający przycisk dodawania.
                Spacer(Modifier.height(Sizes.floatingButton))
            }
        }
        if (hasAlarms) {
            AppIconButton(
                icon = R.drawable.ic_add,
                contentDescription = stringResource(R.string.action_add_alarm),
                onClick = onAddAlarm,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = Spacing.screenEdge, bottom = Sizes.bottomBarClearance)
                    .entranceFrom(rememberEntrance(order = 1)),
                style = AppButtonStyle.Primary,
                size = Sizes.floatingButton,
            )
        }
    }
}

@Composable
private fun NextAlarmPill(timeUntil: TimeUntil?, modifier: Modifier = Modifier) {
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
            text = nextAlarmText(timeUntil),
            style = MaterialTheme.typography.labelMedium,
            color = Accent,
        )
    }
}

/** Duża karta najbliższego alarmu, w kolorze jego przedmiotu. */
@Composable
private fun HeroAlarmCard(
    alarm: Alarm,
    onClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visual = ObjectVisual.of(alarm.requiredObject)
    val objectName = stringResource(visual.nameRes)

    AppCard(
        modifier = modifier.fillMaxWidth(),
        tint = visual.tint,
        shape = AppShapes.heroCard,
        contentPadding = PaddingValues(
            start = Spacing.screenEdge,
            end = Spacing.screenEdge,
            top = Spacing.screenEdge,
            bottom = Spacing.extraSmall,
        ),
        onClick = onClick,
        onClickLabel = stringResource(R.string.alarms_open),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                TimeDisplay(hour = alarm.hour, minute = alarm.minute)
                Text(
                    text = alarmSubtitle(alarm),
                    style = MaterialTheme.typography.bodyLarge,
                    color = visual.tint.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // Na wąskim ekranie ilustracja się zmniejsza, żeby zostawić miejsce na godzinę.
            ObjectImage(
                visual = visual,
                contentDescription = objectName,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .sizeIn(maxWidth = Sizes.illustrationHero, maxHeight = Sizes.illustrationHero)
                    .aspectRatio(1f),
            )
        }
        HorizontalDivider(color = visual.tint.border)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                // Nazwa przedmiotu w środku zdania zaczyna się małą literą: „Zdjęcie: szczoteczka do zębów”.
                text = stringResource(R.string.alarms_task, objectName.replaceFirstChar { it.lowercase() }),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            AppToggle(
                checked = alarm.enabled,
                onCheckedChange = onEnabledChange,
                contentDescription = stringResource(R.string.alarms_toggle),
            )
        }
    }
}

/** Zwykła karta alarmu. Wyłączony alarm jest przygaszony. */
@Composable
private fun AlarmCard(
    alarm: Alarm,
    onClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visual = ObjectVisual.of(alarm.requiredObject)
    val contentAlpha by animateFloatAsState(if (alarm.enabled) 1f else DisabledCardAlpha, label = "alarm alpha")

    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Spacing.screenEdge,
            end = Spacing.smallMedium,
            top = Spacing.smallMedium,
            bottom = Spacing.smallMedium,
        ),
        onClick = onClick,
        onClickLabel = stringResource(R.string.alarms_open),
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
                TimeDisplay(hour = alarm.hour, minute = alarm.minute, size = TimeDisplaySize.Medium)
                Text(
                    text = alarmSubtitle(alarm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                checked = alarm.enabled,
                onCheckedChange = onEnabledChange,
                contentDescription = stringResource(R.string.alarms_toggle),
            )
        }
    }
}

/** Pusty stan: zachęta do dodania pierwszego alarmu. Dopracujemy go w Etapie 7. */
@Composable
private fun EmptyAlarms(onAddAlarm: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        tint = MintTint,
        shape = AppShapes.heroCard,
    ) {
        ObjectImage(
            visual = ObjectVisual.Toothbrush,
            contentDescription = null,
            modifier = Modifier
                .size(Sizes.illustrationCard)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            text = stringResource(R.string.alarms_empty_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.alarms_empty_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MintTint.secondaryText,
        )
        AppButton(
            text = stringResource(R.string.alarms_empty_action),
            onClick = onAddAlarm,
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.ic_add,
        )
    }
}

// Przykładowe alarmy tylko do podglądu. Etykiety to dane wpisane przez użytkownika, więc nie ma ich w strings.xml.
private val previewAlarms = listOf(
    Alarm(id = 1, hour = 7, minute = 30, days = Weekdays.workdays, label = "Zajęcia"),
    Alarm(
        id = 2,
        hour = 9,
        minute = 0,
        days = Weekdays.weekend,
        enabled = false,
        requiredObject = RequiredObject.SECOND,
    ),
    Alarm(
        id = 3,
        hour = 6,
        minute = 15,
        days = setOf(DayOfWeek.WEDNESDAY),
        label = "Siłownia",
        requiredObject = RequiredObject.RANDOM,
    ),
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AlarmsScreenPreview() {
    ShotClockTheme {
        AppBackground {
            AlarmsScreen(
                uiState = AlarmsUiState(
                    isLoading = false,
                    nextAlarm = previewAlarms.first(),
                    timeUntilNext = TimeUntil(days = 0, hours = 7, minutes = 20),
                    otherAlarms = previewAlarms.drop(1),
                ),
                onAddAlarm = {},
                onOpenAlarm = {},
                onEnabledChange = { _, _ -> },
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun AlarmsScreenEmptyPreview() {
    ShotClockTheme {
        AppBackground {
            AlarmsScreen(
                uiState = AlarmsUiState(isLoading = false),
                onAddAlarm = {},
                onOpenAlarm = {},
                onEnabledChange = { _, _ -> },
            )
        }
    }
}
