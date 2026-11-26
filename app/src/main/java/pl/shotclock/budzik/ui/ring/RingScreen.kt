package pl.shotclock.budzik.ui.ring

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.shotclock.budzik.R
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.ui.camera.CameraScreen
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.PulsingHalo
import pl.shotclock.budzik.ui.components.TimeDisplay
import pl.shotclock.budzik.ui.components.TimeDisplaySize
import pl.shotclock.budzik.ui.objects.ObjectImage
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.SectionLabelText
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.ui.theme.rememberPulse
import pl.shotclock.budzik.ui.theme.rememberShakeAngle
import pl.shotclock.budzik.ui.theme.ScreenTransitionMillis
import pl.shotclock.budzik.viewmodel.AppViewModelProvider
import pl.shotclock.budzik.viewmodel.RingUiState
import pl.shotclock.budzik.viewmodel.RingViewModel

private const val BlinkMillis = 550

/**
 * Ekran dzwonienia z ViewModelem. Najpierw pokazuje dzwoniący alarm, a po „Otwórz kamerę” ekran kamery.
 * Gdy alarm zostanie wyłączony, wywołuje [onFinished], żeby zamknąć okno.
 */
@Composable
fun RingRoute(
    onFinished: () -> Unit,
    viewModel: RingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCamera by rememberSaveable { mutableStateOf(false) }

    // Nie ma drzemki ani ucieczki: Wstecz na ekranie dzwonienia nic nie robi, a z kamery wraca do dzwonienia.
    BackHandler { showCamera = false }

    val state = uiState
    if (state == null) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }
    Crossfade(
        targetState = showCamera,
        animationSpec = tween(ScreenTransitionMillis),
        label = "ring or camera",
    ) { camera ->
        if (camera) {
            CameraScreen(onBack = { showCamera = false }, onDismissAlarm = viewModel::dismiss)
        } else {
            RingScreen(uiState = state, onOpenCamera = { showCamera = true })
        }
    }
}

/** Wygląd ekranu dzwonienia. Celowo nie ma tu przycisku drzemki. */
@Composable
fun RingScreen(
    uiState: RingUiState,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visual = ObjectVisual.of(uiState.objectToFind)

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium, Alignment.CenterVertically),
    ) {
        RingStatus(Modifier.entranceFrom(rememberEntrance(order = 0)))
        Column(
            modifier = Modifier.entranceFrom(rememberEntrance(order = 1)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TimeDisplay(hour = uiState.hour, minute = uiState.minute, size = TimeDisplaySize.Huge)
            if (uiState.label.isNotBlank()) {
                Text(
                    text = uiState.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        // Na niskim ekranie pole z falami się zmniejsza, żeby przycisk kamery zawsze był widoczny.
        RingingObject(
            visual = visual,
            modifier = Modifier
                .weight(1f, fill = false)
                .entranceFrom(rememberEntrance(order = 2)),
        )
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .entranceFrom(rememberEntrance(order = 3)),
        ) {
            Text(
                text = stringResource(R.string.ring_task),
                style = SectionLabelText,
                color = TextSecondary,
            )
            Text(
                text = stringResource(visual.nameRes),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Column(
            modifier = Modifier.entranceFrom(rememberEntrance(order = 4)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
        ) {
            AppButton(
                text = stringResource(R.string.ring_open_camera),
                onClick = onOpenCamera,
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.ic_camera,
            )
            Text(
                text = stringResource(R.string.ring_note),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RingStatus(modifier: Modifier = Modifier) {
    val blink = rememberPulse(BlinkMillis, label = "status blink")

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.statusDot)
                // Przy wyłączonych animacjach w telefonie wartość zatrzymuje się na 1, więc kropka zostaje w pełni widoczna.
                .graphicsLayer { alpha = 0.25f + 0.75f * blink.value }
                .clip(AppShapes.pill)
                .background(Accent),
        )
        Text(text = stringResource(R.string.ring_status), style = SectionLabelText, color = Accent)
    }
}

@Composable
private fun RingingObject(visual: ObjectVisual, modifier: Modifier = Modifier) {
    val shake = rememberShakeAngle()

    PulsingHalo(
        modifier
            .sizeIn(maxWidth = Sizes.ringHalo, maxHeight = Sizes.ringHalo)
            .aspectRatio(1f, matchHeightConstraintsFirst = true),
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.ringCircle)
                .clip(AppShapes.pill)
                .background(visual.tint.surface)
                .border(Sizes.cardBorder, visual.tint.border, AppShapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            ObjectImage(
                visual = visual,
                contentDescription = stringResource(visual.nameRes),
                modifier = Modifier
                    .size(Sizes.ringObject)
                    .graphicsLayer { rotationZ = shake.value },
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun RingScreenPreview() {
    ShotClockTheme {
        AppBackground {
            RingScreen(uiState = sampleRingUiState(), onOpenCamera = {})
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun RingScreenMugPreview() {
    ShotClockTheme {
        AppBackground {
            RingScreen(
                uiState = RingUiState(hour = 6, minute = 5, label = "", objectToFind = RequiredObject.SECOND),
                onOpenCamera = {},
            )
        }
    }
}

/** Przykładowy alarm do podglądów i do tymczasowego przycisku „Dzwonienie” w ustawieniach. */
@Composable
fun sampleRingUiState() = RingUiState(
    hour = 7,
    minute = 30,
    label = stringResource(R.string.ring_label_sample),
    objectToFind = RequiredObject.FIRST,
)
