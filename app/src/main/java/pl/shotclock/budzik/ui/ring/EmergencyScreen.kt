package pl.shotclock.budzik.ui.ring

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.shotclock.budzik.R
import pl.shotclock.budzik.alarm.MathTask
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButtonStyle
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.AppIconButton
import pl.shotclock.budzik.ui.components.buttonContentColor
import pl.shotclock.budzik.ui.components.buttonSurface
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.LilacTint
import pl.shotclock.budzik.ui.theme.SectionLabelText
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.TimeTextMedium
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.ui.theme.rememberPressScale
import pl.shotclock.budzik.ui.theme.scaledBy
import pl.shotclock.budzik.viewmodel.AppViewModelProvider
import pl.shotclock.budzik.viewmodel.EmergencyUiState
import pl.shotclock.budzik.viewmodel.EmergencyViewModel

/**
 * Wyjście awaryjne z ViewModelem. Alarm dzwoni dalej, dopóki wynik nie będzie poprawny.
 * Po dobrym wyniku wywołuje [onSolved] (RingRoute wyłącza wtedy alarm i pokazuje sukces).
 */
@Composable
fun EmergencyRoute(
    onBack: () -> Unit,
    onSolved: () -> Unit,
    viewModel: EmergencyViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    EmergencyScreen(
        uiState = uiState,
        onBack = onBack,
        onDigit = viewModel::onDigit,
        onDelete = viewModel::onDelete,
        onSubmit = { if (viewModel.onSubmit()) onSolved() },
    )
}

/**
 * Wygląd wyjścia awaryjnego: zadanie w karcie w kolorze „losowego” przedmiotu, wpisany wynik i własna
 * klawiatura numeryczna. Własna, a nie systemowa, bo działa tak samo na zablokowanym ekranie i nie zasłania zadania.
 */
@Composable
fun EmergencyScreen(
    uiState: EmergencyUiState,
    onBack: () -> Unit,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Na wysokim ekranie: nagłówek na górze, zadanie w środku, klawiatura na dole.
    // Na niskim ekranie (albo przy dużej czcionce) wszystko układa się jedno pod drugim i da się przewinąć.
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = Spacing.screenEdge, vertical = Spacing.medium),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            EmergencyHeader(onBack, Modifier.entranceFrom(rememberEntrance(order = 0)))
            TaskCard(
                uiState = uiState,
                modifier = Modifier
                    .padding(vertical = Spacing.large)
                    .entranceFrom(rememberEntrance(order = 1)),
            )
            Column(
                modifier = Modifier.entranceFrom(rememberEntrance(order = 2)),
                verticalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
            ) {
                NumberPad(onDigit = onDigit, onDelete = onDelete, onSubmit = onSubmit)
                Text(
                    text = stringResource(R.string.emergency_note),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun EmergencyHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        AppIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        Text(stringResource(R.string.emergency_title), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun TaskCard(uiState: EmergencyUiState, modifier: Modifier = Modifier) {
    val task = uiState.task
    val answerDescription = if (uiState.answer.isEmpty()) {
        stringResource(R.string.emergency_answer_description_empty)
    } else {
        stringResource(R.string.emergency_answer_description, uiState.answer)
    }

    AppCard(modifier = modifier.fillMaxWidth(), tint = LilacTint, shape = AppShapes.heroCard) {
        // Po złej odpowiedzi komunikat zastępuje podpis, żeby karta nie rosła (ważne na niskim ekranie).
        // liveRegion: czytnik ekranu sam przeczyta komunikat o złym wyniku.
        Text(
            text = stringResource(if (uiState.wrongAnswer) R.string.emergency_wrong else R.string.emergency_task_label),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = SectionLabelText,
            color = if (uiState.wrongAnswer) TextPrimary else LilacTint.secondaryText,
        )
        Text(
            text = stringResource(R.string.emergency_task, task.first, task.second, task.third),
            style = TimeTextMedium,
        )
        // Pole z wynikiem: pusty znak zapytania, a po wpisaniu cyfry w kolorze akcentu.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.answerField)
                .clip(AppShapes.tile)
                .border(Sizes.cardBorder, LilacTint.border, AppShapes.tile)
                .clearAndSetSemantics { contentDescription = answerDescription },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = uiState.answer.ifEmpty { stringResource(R.string.emergency_answer_empty) },
                style = TimeTextMedium,
                color = if (uiState.answer.isEmpty()) LilacTint.secondaryText else Accent,
            )
        }
    }
}

/** Klawiatura 3 × 4: cyfry, „usuń cyfrę” i „sprawdź wynik”. */
@Composable
private fun NumberPad(
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                row.forEach { digit ->
                    KeypadKey(onClick = { onDigit(digit) }, modifier = Modifier.weight(1f)) {
                        Text(digit.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            KeypadIconKey(
                icon = R.drawable.ic_backspace,
                description = stringResource(R.string.emergency_delete),
                onClick = onDelete,
                modifier = Modifier.weight(1f),
            )
            KeypadKey(onClick = { onDigit(0) }, modifier = Modifier.weight(1f)) {
                Text("0", style = MaterialTheme.typography.titleLarge)
            }
            KeypadIconKey(
                icon = R.drawable.ic_check,
                description = stringResource(R.string.emergency_submit),
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                style = AppButtonStyle.Primary,
            )
        }
    }
}

/** Jeden klawisz: kafelek w stylu przycisków aplikacji, sprężyście reaguje na dotyk. */
@Composable
private fun KeypadKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Secondary,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)

    Box(
        modifier = modifier
            .scaledBy(scale)
            .height(Sizes.keypadKey)
            .buttonSurface(style, AppShapes.tile)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun KeypadIconKey(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Secondary,
) {
    KeypadKey(onClick = onClick, modifier = modifier, style = style) {
        Icon(painterResource(icon), contentDescription = description, tint = buttonContentColor(style))
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun EmergencyScreenPreview() {
    ShotClockTheme {
        AppBackground {
            EmergencyScreen(
                uiState = EmergencyUiState(task = MathTask(23, 7, 48), answer = "20"),
                onBack = {},
                onDigit = {},
                onDelete = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun EmergencyScreenWrongAnswerPreview() {
    ShotClockTheme {
        AppBackground {
            EmergencyScreen(
                uiState = EmergencyUiState(task = MathTask(41, 6, 17), wrongAnswer = true),
                onBack = {},
                onDigit = {},
                onDelete = {},
                onSubmit = {},
            )
        }
    }
}
