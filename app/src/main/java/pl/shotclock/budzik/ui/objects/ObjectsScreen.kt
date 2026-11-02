package pl.shotclock.budzik.ui.objects

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.HighlightColor
import pl.shotclock.budzik.ui.theme.OnHighlight
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.ui.theme.rememberPulse

private const val FloatMillis = 2000
private const val FloatDistancePx = 16f
private const val FloatAngle = 4f

/** Przedmioty na przykładowych danych. Liczba alarmów i wersja modelu dojdą w Etapie 6. */
@Composable
fun ObjectsScreen(modifier: Modifier = Modifier) {
    ScreenContainer(
        title = stringResource(R.string.tab_objects),
        modifier = modifier,
        hasBottomBar = true,
    ) {
        Text(
            text = stringResource(R.string.objects_description),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        ObjectCard(
            visual = ObjectVisual.Toothbrush,
            tipRes = R.string.object_toothbrush_tip,
            modifier = Modifier.entranceFrom(rememberEntrance(order = 0)),
        )
        ObjectCard(
            visual = ObjectVisual.Mug,
            tipRes = R.string.object_mug_tip,
            modifier = Modifier.entranceFrom(rememberEntrance(order = 1)),
        )
        ModelStatusCard(Modifier.entranceFrom(rememberEntrance(order = 2)))
    }
}

@Composable
private fun ObjectCard(
    visual: ObjectVisual,
    @StringRes tipRes: Int,
    modifier: Modifier = Modifier,
) {
    val float = rememberPulse(FloatMillis, label = "object float")

    AppCard(
        modifier = modifier.fillMaxWidth(),
        tint = visual.tint,
        shape = AppShapes.heroCard,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Text(
                    text = stringResource(visual.nameRes),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(tipRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = visual.tint.secondaryText,
                )
                Text(
                    text = stringResource(R.string.objects_used_sample),
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(HighlightColor)
                        .padding(horizontal = Spacing.smallMedium, vertical = Spacing.extraSmall),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnHighlight,
                )
            }
            ObjectImage(
                visual = visual,
                contentDescription = stringResource(visual.nameRes),
                modifier = Modifier
                    .size(Sizes.illustrationCard)
                    .graphicsLayer {
                        translationY = (float.value - 0.5f) * FloatDistancePx
                        rotationZ = (float.value - 0.5f) * FloatAngle
                    },
            )
        }
    }
}

@Composable
private fun ModelStatusCard(modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
        ) {
            Box(
                modifier = Modifier
                    .size(Sizes.modelDot)
                    .clip(AppShapes.pill)
                    .background(Accent),
            )
            Text(
                text = stringResource(R.string.objects_model),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.objects_model_test),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ObjectsScreenPreview() {
    ShotClockTheme {
        AppBackground {
            ObjectsScreen()
        }
    }
}
