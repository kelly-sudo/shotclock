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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
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
import pl.shotclock.budzik.ui.components.AppSnackbar
import pl.shotclock.budzik.ui.components.AppToggle
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.components.TimeDisplay
import pl.shotclock.budzik.ui.components.TimeDisplaySize
import pl.shotclock.budzik.ui.objects.ObjectImage
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ControlOffColor
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.viewmodel.AlarmsUiState
import pl.shotclock.budzik.viewmodel.AlarmsViewModel
import pl.shotclock.budzik.viewmodel.AppViewModelProvider

private const val DisabledCardAlpha = 0.6f
// Przy długiej liście dalsze karty wjeżdżają razem, żeby nie czekać na nie zbyt długo.
private const val MaxEntranceOrder = 6

/** Akcje, które lista alarmów przekazuje dalej. Zebrane w jednym miejscu, żeby nie podawać ich osobno. */
data class AlarmsActions(
    val onAddAlarm: () -> Unit = {},
    val onOpenAlarm: (alarmId: Long) -> Unit = {},
    val onEnabledChange: (Alarm, Boolean) -> Unit = { _, _ -> },
    val onDelete: (alarmId: Long) -> Unit = {},
    val onUndoDelete: () -> Unit = {},
    val onUndoDismissed: () -> Unit = {},
)

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
        actions = AlarmsActions(
            onAddAlarm = onAddAlarm,
            onOpenAlarm = onOpenAlarm,
            onEnabledChange = viewModel::setEnabled,
            onDelete = viewModel::delete,
            onUndoDelete = viewModel::undoDelete,
            onUndoDismissed = viewModel::forgetDeleted,
        ),
    )
}

/** Wygląd listy alarmów. Dostaje gotowy stan, więc działa też w @Preview, bez bazy. */
@Composable
fun AlarmsScreen(
    uiState: AlarmsUiState,
    actions: AlarmsActions,
    modifier: Modifier = Modifier,
) {
    val hasAlarms = !uiState.isLoading && !uiState.isEmpty
    val snackbarHostState = remember { SnackbarHostState() }
    val deleted = uiState.recentlyDeleted
    val deletedMessage = deleted?.let { stringResource(R.string.alarms_deleted, formatTime(it.hour, it.minute)) }
    val undoLabel = stringResource(R.string.alarms_undo)

    // Po usunięciu alarmu pokazujemy pasek z „Cofnij”. Jeśli zniknie bez kliknięcia, usunięcie zostaje.
    LaunchedEffect(deleted) {
        if (deletedMessage == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = deletedMessage,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) actions.onUndoDelete() else actions.onUndoDismissed()
    }

    Box(modifier.fillMaxSize()) {
        ScreenContainer(title = stringResource(R.string.tab_alarms), hasBottomBar = true) {
            if (uiState.isEmpty) {
                EmptyAlarms(actions.onAddAlarm, Modifier.entranceFrom(rememberEntrance(order = 0)))
            }
            if (hasAlarms) {
                NextAlarmPill(uiState.timeUntilNext, Modifier.entranceFrom(rememberEntrance(order = 0)))
                uiState.nextAlarm?.let { alarm ->
                    // key sprawia, że stan karty (np. animacja) należy do konkretnego alarmu, a nie do miejsca na liście.
                    key(alarm.id) {
                        SwipeToDelete(
                            shape = AppShapes.heroCard,
                            onDelete = { actions.onDelete(alarm.id) },
                            modifier = Modifier.entranceFrom(rememberEntrance(order = 1)),
                        ) {
                            HeroAlarmCard(
                                alarm = alarm,
                                onClick = { actions.onOpenAlarm(alarm.id) },
                                onEnabledChange = { actions.onEnabledChange(alarm, it) },
                            )
                        }
                    }
                }
                uiState.otherAlarms.forEachIndexed { index, alarm ->
                    key(alarm.id) {
                        SwipeToDelete(
                            shape = AppShapes.card,
                            onDelete = { actions.onDelete(alarm.id) },
                            modifier = Modifier.entranceFrom(
                                rememberEntrance(order = minOf(index + 2, MaxEntranceOrder)),
                            ),
                        ) {
                            AlarmCard(
                                alarm = alarm,
                                onClick = { actions.onOpenAlarm(alarm.id) },
                                onEnabledChange = { actions.onEnabledChange(alarm, it) },
                            )
                        }
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
                onClick = actions.onAddAlarm,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = Spacing.screenEdge, bottom = Sizes.bottomBarClearance)
                    .entranceFrom(rememberEntrance(order = 1)),
                style = AppButtonStyle.Primary,
                size = Sizes.floatingButton,
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            // Pasek pojawia się nad przyciskiem dodawania, żeby go nie zasłaniać.
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = Spacing.screenEdge)
                .padding(bottom = Sizes.bottomBarClearance + Sizes.floatingButton + Spacing.small),
        ) { data ->
            AppSnackbar(
                message = data.visuals.message,
                actionLabel = data.visuals.actionLabel,
                onAction = { data.performAction() },
            )
        }
    }
}

/**
 * Karta, którą można przesunąć w lewo, żeby usunąć alarm. Pod spodem widać kosz.
 * Czytnik ekranu nie obsłuży przesunięcia, więc ta sama akcja jest też w menu czynności karty.
 */
@Composable
private fun SwipeToDelete(
    shape: Shape,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()
    val deleteLabel = stringResource(R.string.action_delete_alarm)

    SwipeToDismissBox(
        state = state,
        modifier = modifier.semantics {
            customActions = listOf(CustomAccessibilityAction(deleteLabel) { onDelete(); true })
        },
        enableDismissFromStartToEnd = false,
        onDismiss = { value -> if (value == SwipeToDismissBoxValue.EndToStart) onDelete() },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(ControlOffColor)
                    .padding(end = Spacing.large),
                contentAlignment = Alignment.CenterEnd,
            ) {
                // Opis jest w akcji karty, więc sama ikona go nie potrzebuje.
                Icon(painterResource(R.drawable.ic_delete), contentDescription = null, tint = TextPrimary)
            }
        },
    ) {
        content()
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
                text = alarmTaskText(alarm),
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
                // Ta sama informacja co na dużej karcie, tylko ciszej, żeby karta najbliższego alarmu nadal się wyróżniała.
                // Na wąskim ekranie „Zdjęcie: losowy przedmiot” łamie się na dwie linie, zamiast się uciąć.
                Text(
                    text = alarmTaskText(alarm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
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
                actions = AlarmsActions(),
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
                actions = AlarmsActions(),
            )
        }
    }
}
