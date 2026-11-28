package pl.shotclock.budzik.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import pl.shotclock.budzik.R
import pl.shotclock.budzik.data.AppSettings
import pl.shotclock.budzik.data.ConfidenceThresholdOptions
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.ml.ClassifierMode
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppButtonStyle
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.AppToggle
import pl.shotclock.budzik.ui.components.OptionRow
import pl.shotclock.budzik.ui.components.PresetChip
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.edit.rememberSoundPicker
import pl.shotclock.budzik.ui.edit.soundTitle
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.SectionLabelText
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.viewmodel.AppViewModelProvider
import pl.shotclock.budzik.viewmodel.SettingsUiState
import pl.shotclock.budzik.viewmodel.SettingsViewModel

// Dwa progi uznajemy za równe, gdy różnią się mniej niż o to (liczby Float nie są dokładne).
private const val ThresholdTolerance = 0.001f

/** Akcje ekranu ustawień zebrane w jednym miejscu, żeby nie podawać ich osobno. */
data class SettingsActions(
    val onDefaultSoundChange: (String?) -> Unit = {},
    val onDefaultVibrationChange: (Boolean) -> Unit = {},
    val onTestModeChange: (Boolean) -> Unit = {},
    val onThresholdChange: (Float) -> Unit = {},
    val onPreviewObjectChange: (RequiredObject) -> Unit = {},
    val onVersionTap: () -> Unit = {},
    val onDeveloperModeOff: () -> Unit = {},
    // Podgląd ekranów (tylko w trybie deweloperskim), dopóki nie mają prawdziwych wyzwalaczy.
    val onOpenOnboarding: () -> Unit = {},
    val onOpenRing: () -> Unit = {},
    val onOpenCamera: () -> Unit = {},
)

/** Ekran ustawień z ViewModelem. */
@Composable
fun SettingsRoute(
    onOpenOnboarding: () -> Unit,
    onOpenRing: () -> Unit,
    onOpenCamera: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        actions = SettingsActions(
            onDefaultSoundChange = viewModel::setDefaultSound,
            onDefaultVibrationChange = viewModel::setDefaultVibration,
            onTestModeChange = viewModel::setTestMode,
            onThresholdChange = viewModel::setConfidenceThreshold,
            onPreviewObjectChange = viewModel::setPreviewObject,
            onVersionTap = viewModel::onVersionTapped,
            onDeveloperModeOff = viewModel::turnOffDeveloperMode,
            onOpenOnboarding = onOpenOnboarding,
            onOpenRing = onOpenRing,
            onOpenCamera = onOpenCamera,
        ),
    )
}

/** Wygląd ustawień. Dostaje gotowy stan, więc działa też w @Preview, bez DataStore. */
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    ScreenContainer(
        title = stringResource(R.string.tab_settings),
        modifier = modifier,
        hasBottomBar = true,
    ) {
        SectionTitle(R.string.settings_section_alarm, Modifier.entranceFrom(rememberEntrance(order = 0)))
        AlarmDefaultsCard(uiState.settings, actions, Modifier.entranceFrom(rememberEntrance(order = 1)))
        SectionTitle(R.string.settings_section_recognition, Modifier.entranceFrom(rememberEntrance(order = 2)))
        RecognitionCard(uiState, actions, Modifier.entranceFrom(rememberEntrance(order = 3)))
        // Sekcja pojawia się dopiero po 7 dotknięciach wersji, więc nie ma animacji wjazdu.
        if (uiState.settings.developerMode) {
            SectionTitle(R.string.settings_section_developer)
            DeveloperCard(uiState.settings, actions)
        }
        SectionTitle(R.string.settings_section_about, Modifier.entranceFrom(rememberEntrance(order = 4)))
        AboutCard(uiState, actions, Modifier.entranceFrom(rememberEntrance(order = 5)))
    }
}

@Composable
private fun SectionTitle(@StringRes textRes: Int, modifier: Modifier = Modifier) {
    Text(text = stringResource(textRes), modifier = modifier, style = SectionLabelText, color = TextSecondary)
}

/** Karta z wierszami opcji: wiersze stoją jeden pod drugim, o rytmie decyduje stała wysokość wiersza (jak w edycji). */
@Composable
private fun OptionsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Spacing.medium,
            end = Spacing.small,
            top = Spacing.extraSmall,
            bottom = Spacing.small,
        ),
        verticalSpacing = Spacing.none,
        content = content,
    )
}

/** Opis pod wierszami karty. Prawy odstęp wyrównuje go z przełącznikami. */
@Composable
private fun CardHint(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(end = Spacing.small, bottom = Spacing.small),
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
    )
}

@Composable
private fun AlarmDefaultsCard(settings: AppSettings, actions: SettingsActions, modifier: Modifier = Modifier) {
    OptionsCard(modifier) {
        val pickSound = rememberSoundPicker(settings.defaultSoundUri, actions.onDefaultSoundChange)
        OptionRow(
            label = stringResource(R.string.settings_default_sound),
            icon = R.drawable.ic_music,
            onClick = pickSound,
            onClickLabel = stringResource(R.string.edit_sound_change),
        ) {
            Text(
                text = soundTitle(settings.defaultSoundUri),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                // Długa nazwa dzwonka nie może zepchnąć napisu „Domyślny dźwięk”.
                modifier = Modifier.widthIn(max = Sizes.soundTitleMaxWidth),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = TextSecondary)
        }
        val vibration = stringResource(R.string.settings_default_vibration)
        OptionRow(label = vibration, icon = R.drawable.ic_vibration) {
            AppToggle(
                checked = settings.defaultVibration,
                onCheckedChange = actions.onDefaultVibrationChange,
                contentDescription = vibration,
            )
        }
        CardHint(stringResource(R.string.settings_defaults_hint))
    }
}

@Composable
private fun RecognitionCard(uiState: SettingsUiState, actions: SettingsActions, modifier: Modifier = Modifier) {
    OptionsCard(modifier) {
        val testMode = stringResource(R.string.settings_test_mode)
        OptionRow(
            label = testMode,
            icon = R.drawable.ic_camera,
            description = stringResource(R.string.settings_test_mode_description),
        ) {
            AppToggle(
                checked = uiState.settings.testMode,
                onCheckedChange = actions.onTestModeChange,
                contentDescription = testMode,
            )
        }
        // Tryb testowy jest wyłączony, ale modelu jeszcze nie ma: mówimy wprost, co się dzieje.
        if (uiState.classifierMode == ClassifierMode.NoModel) {
            CardHint(stringResource(R.string.settings_model_missing_hint))
        }
    }
}

@Composable
private fun DeveloperCard(settings: AppSettings, actions: SettingsActions) {
    val threshold = settings.confidenceThreshold
    AppCard(Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.settings_threshold), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(R.string.settings_threshold_description),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            ConfidenceThresholdOptions.forEach { option ->
                PresetChip(
                    label = stringResource(R.string.settings_threshold_value, (option * 100).roundToInt()),
                    selected = abs(option - threshold) < ThresholdTolerance,
                    onClick = { actions.onThresholdChange(option) },
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_preview_title),
            modifier = Modifier.padding(top = Spacing.small),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.settings_preview_description),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        SecondaryButton(stringResource(R.string.settings_preview_onboarding), actions.onOpenOnboarding)
        SecondaryButton(stringResource(R.string.settings_preview_ring), actions.onOpenRing)
        Text(
            text = stringResource(R.string.settings_preview_object),
            modifier = Modifier.padding(top = Spacing.small),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.settings_preview_object_description),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            listOf(RequiredObject.FIRST, RequiredObject.SECOND).forEach { option ->
                PresetChip(
                    label = stringResource(ObjectVisual.of(option).nameRes),
                    selected = option == settings.previewObject,
                    onClick = { actions.onPreviewObjectChange(option) },
                )
            }
        }
        SecondaryButton(stringResource(R.string.settings_preview_camera), actions.onOpenCamera)
        SecondaryButton(stringResource(R.string.settings_developer_off), actions.onDeveloperModeOff)
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        style = AppButtonStyle.Secondary,
    )
}

@Composable
private fun AboutCard(uiState: SettingsUiState, actions: SettingsActions, modifier: Modifier = Modifier) {
    OptionsCard(modifier) {
        // Siedem dotknięć wersji włącza tryb deweloperski, jak „Numer kompilacji” w ustawieniach Androida.
        OptionRow(
            label = stringResource(R.string.settings_version),
            description = uiState.developerTapsLeft?.let {
                pluralStringResource(R.plurals.settings_developer_taps_left, it, it)
            },
            onClick = actions.onVersionTap,
        ) {
            Text(
                text = uiState.appVersion,
                modifier = Modifier.padding(end = Spacing.small),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        OptionRow(label = stringResource(R.string.settings_model_version)) {
            Text(
                text = when (uiState.classifierMode) {
                    ClassifierMode.Test -> stringResource(R.string.objects_model_test)
                    ClassifierMode.NoModel -> stringResource(R.string.objects_model_missing)
                    ClassifierMode.Real -> stringResource(R.string.objects_model_version, uiState.modelVersion)
                },
                modifier = Modifier.padding(end = Spacing.small),
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.classifierMode == ClassifierMode.Real) Accent else TextSecondary,
            )
        }
        CardHint(stringResource(R.string.settings_about_description))
        CardHint(stringResource(R.string.settings_license))
    }
}

@Preview(widthDp = 390, heightDp = 1000)
@Composable
private fun SettingsScreenPreview() {
    ShotClockTheme {
        AppBackground {
            SettingsScreen(
                uiState = SettingsUiState(appVersion = "1.0", modelVersion = "1.0", developerTapsLeft = 2),
                actions = SettingsActions(),
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 1500)
@Composable
private fun SettingsScreenDeveloperPreview() {
    ShotClockTheme {
        AppBackground {
            SettingsScreen(
                uiState = SettingsUiState(
                    settings = AppSettings(testMode = false, developerMode = true),
                    classifierMode = ClassifierMode.NoModel,
                    appVersion = "1.0",
                    modelVersion = "1.0",
                ),
                actions = SettingsActions(),
            )
        }
    }
}
