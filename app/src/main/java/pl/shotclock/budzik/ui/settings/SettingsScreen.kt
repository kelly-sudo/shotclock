package pl.shotclock.budzik.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppButtonStyle
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.AppToggle
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.TextSecondary

/**
 * Zaślepka ustawień. Właściwe ustawienia powstaną w Etapie 6.
 *
 * Sekcja „Podgląd ekranów” jest tymczasowa: usuniemy ją, gdy dzwonienie, kamera
 * i onboarding dostaną swoje prawdziwe wyzwalacze (Etapy 3, 5 i 7).
 */
@Composable
fun SettingsScreen(
    onOpenOnboarding: () -> Unit,
    onOpenRing: () -> Unit,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var vibration by rememberSaveable { mutableStateOf(true) }

    ScreenContainer(
        title = stringResource(R.string.tab_settings),
        modifier = modifier,
        hasBottomBar = true,
    ) {
        AppCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.settings_vibration_sample),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                AppToggle(
                    checked = vibration,
                    onCheckedChange = { vibration = it },
                    contentDescription = stringResource(R.string.settings_vibration_sample),
                )
            }
            Text(
                text = stringResource(R.string.settings_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        AppCard(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_preview_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_preview_description),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            PreviewButton(stringResource(R.string.settings_preview_onboarding), onOpenOnboarding)
            PreviewButton(stringResource(R.string.settings_preview_ring), onOpenRing)
            PreviewButton(stringResource(R.string.settings_preview_camera), onOpenCamera)
        }
    }
}

@Composable
private fun PreviewButton(text: String, onClick: () -> Unit) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        style = AppButtonStyle.Secondary,
    )
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenPreview() {
    ShotClockTheme {
        AppBackground {
            SettingsScreen(onOpenOnboarding = {}, onOpenRing = {}, onOpenCamera = {})
        }
    }
}
