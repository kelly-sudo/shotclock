package pl.shotclock.budzik.ui.history

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

/** Zaślepka historii. Lista pobudek i statystyki powstaną w Etapie 6. */
@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    ScreenContainer(
        title = stringResource(R.string.tab_history),
        modifier = modifier,
        hasBottomBar = true,
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.history_placeholder),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun HistoryScreenPreview() {
    ShotClockTheme {
        GlassBackground {
            HistoryScreen()
        }
    }
}
