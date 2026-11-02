package pl.shotclock.budzik.ui.camera

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.GlassBackground
import pl.shotclock.budzik.ui.components.GlassCard
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.theme.ShotClockTheme

/** Zaślepka ekranu kamery. Podgląd i rozpoznawanie przedmiotu powstaną w Etapie 4. */
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContainer(
        title = stringResource(R.string.camera_title),
        modifier = modifier,
        onBack = onBack,
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.camera_placeholder),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun CameraScreenPreview() {
    ShotClockTheme {
        GlassBackground {
            CameraScreen(onBack = {})
        }
    }
}
