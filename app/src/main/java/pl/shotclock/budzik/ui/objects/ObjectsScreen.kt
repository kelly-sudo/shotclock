package pl.shotclock.budzik.ui.objects

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.shotclock.budzik.R
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.ml.ClassifierMode
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.ui.theme.rememberPulse
import pl.shotclock.budzik.viewmodel.AppViewModelProvider
import pl.shotclock.budzik.viewmodel.ObjectsUiState
import pl.shotclock.budzik.viewmodel.ObjectsViewModel

private const val FloatMillis = 2000
private const val FloatDistancePx = 16f
private const val FloatAngle = 4f

/** Ekran przedmiotów z ViewModelem. */
@Composable
fun ObjectsRoute(viewModel: ObjectsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ObjectsScreen(uiState)
}

/** Dwa przedmioty, które rozpoznaje model, z liczbą alarmów, i stan modelu. Działa też w @Preview, bez bazy. */
@Composable
fun ObjectsScreen(uiState: ObjectsUiState, modifier: Modifier = Modifier) {
    val randomCount = uiState.alarmCounts[RequiredObject.RANDOM] ?: 0
    ScreenContainer(
        title = stringResource(R.string.tab_objects),
        modifier = modifier,
        hasBottomBar = true,
    ) {
        Text(
            text = stringResource(R.string.objects_description),
            modifier = Modifier.entranceFrom(rememberEntrance(order = 0)),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        ObjectCard(
            visual = ObjectVisual.Toothbrush,
            tipRes = R.string.object_toothbrush_tip,
            alarmCount = uiState.alarmCounts[RequiredObject.FIRST] ?: 0,
            randomAlarmCount = randomCount,
            modifier = Modifier.entranceFrom(rememberEntrance(order = 1)),
        )
        ObjectCard(
            visual = ObjectVisual.Mug,
            tipRes = R.string.object_mug_tip,
            alarmCount = uiState.alarmCounts[RequiredObject.SECOND] ?: 0,
            randomAlarmCount = randomCount,
            modifier = Modifier.entranceFrom(rememberEntrance(order = 2)),
            // Kubek unosi się w przeciwnej fazie niż szczoteczka, jak w makiecie.
            floatOffsetMillis = FloatMillis,
        )
        ModelStatusCard(
            mode = uiState.classifierMode,
            version = uiState.modelVersion,
            modifier = Modifier.entranceFrom(rememberEntrance(order = 3)),
        )
    }
}

@Composable
private fun ObjectCard(
    visual: ObjectVisual,
    @StringRes tipRes: Int,
    alarmCount: Int,
    randomAlarmCount: Int,
    modifier: Modifier = Modifier,
    floatOffsetMillis: Int = 0,
) {
    val float = rememberPulse(FloatMillis, label = "object float", startOffsetMillis = floatOffsetMillis)

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
                    text = if (alarmCount > 0) {
                        pluralStringResource(R.plurals.objects_used, alarmCount, alarmCount)
                    } else {
                        stringResource(R.string.objects_used_none)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = visual.tint.secondaryText,
                )
                // Alarm z losowym przedmiotem może wymagać każdego z dwóch, więc mówimy o tym osobno.
                if (randomAlarmCount > 0) {
                    Text(
                        text = pluralStringResource(R.plurals.objects_used_random, randomAlarmCount, randomAlarmCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = visual.tint.secondaryText,
                    )
                }
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
private fun ModelStatusCard(mode: ClassifierMode, version: String, modifier: Modifier = Modifier) {
    // Pasek informacyjny, nie karta z treścią: mniejsze zaokrąglenie i niższy odstęp w pionie, jak w makiecie.
    AppCard(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.tile,
        contentPadding = PaddingValues(horizontal = Spacing.screenEdge, vertical = Spacing.smallMedium),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
        ) {
            // Turkus znaczy „działa”, więc w trybie testowym (bez prawdziwego modelu) kropka jest szara.
            Box(
                modifier = Modifier
                    .size(Sizes.modelDot)
                    .clip(AppShapes.pill)
                    .background(if (mode == ClassifierMode.Real) Accent else TextSecondary),
            )
            Text(
                text = stringResource(R.string.objects_model),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = when (mode) {
                    ClassifierMode.Test -> stringResource(R.string.objects_model_test)
                    ClassifierMode.NoModel -> stringResource(R.string.objects_model_missing)
                    ClassifierMode.Real -> stringResource(R.string.objects_model_version, version)
                },
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
            ObjectsScreen(
                ObjectsUiState(
                    alarmCounts = mapOf(RequiredObject.FIRST to 2, RequiredObject.RANDOM to 1),
                    classifierMode = ClassifierMode.Test,
                    modelVersion = "1.0",
                ),
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 760)
@Composable
private fun ObjectsScreenRealModelPreview() {
    ShotClockTheme {
        AppBackground {
            ObjectsScreen(ObjectsUiState(classifierMode = ClassifierMode.Real, modelVersion = "1.0"))
        }
    }
}
