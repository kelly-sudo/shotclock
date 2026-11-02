package pl.shotclock.budzik.ui.ring

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
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
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.OverlineText
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.ui.theme.rememberPulse
import pl.shotclock.budzik.ui.theme.rememberShakeAngle

private const val BlinkMillis = 550

/**
 * Ekran dzwonienia na przykładowych danych. Prawdziwy alarm dojdzie w Etapie 3.
 * Celowo nie ma tu przycisku drzemki.
 */
@Composable
fun RingScreen(
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            TimeDisplay(hour = 7, minute = 30, size = TimeDisplaySize.Huge)
            Text(
                text = stringResource(R.string.ring_label_sample),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )
        }
        // Na niskim ekranie pole z falami się zmniejsza, żeby przycisk kamery zawsze był widoczny.
        RingingObject(
            Modifier
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
                style = OverlineText,
                color = TextSecondary,
            )
            Text(
                text = stringResource(ObjectVisual.Toothbrush.nameRes),
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
                .graphicsLayer { alpha = 1f - 0.75f * blink.value }
                .clip(AppShapes.pill)
                .background(Accent),
        )
        Text(text = stringResource(R.string.ring_status), style = OverlineText, color = Accent)
    }
}

@Composable
private fun RingingObject(modifier: Modifier = Modifier) {
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
                .background(MintTint.surface)
                .border(Sizes.cardBorder, MintTint.border, AppShapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            ObjectImage(
                visual = ObjectVisual.Toothbrush,
                contentDescription = stringResource(ObjectVisual.Toothbrush.nameRes),
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
            RingScreen(onOpenCamera = {})
        }
    }
}
