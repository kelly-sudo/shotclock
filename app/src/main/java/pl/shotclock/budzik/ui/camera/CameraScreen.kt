package pl.shotclock.budzik.ui.camera

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppCard
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
        AppCard(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.camera_placeholder),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun CameraScreenPreview() {
    ShotClockTheme {
        AppBackground {
            CameraScreen(onBack = {})
        }
    }
}
