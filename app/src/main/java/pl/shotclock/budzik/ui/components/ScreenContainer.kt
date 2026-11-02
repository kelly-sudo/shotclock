package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing

/**
 * Wspólny układ zwykłego ekranu: tytuł, opcjonalny przycisk cofania i przewijana treść
 * z odstępami od pasków systemowych.
 */
@Composable
fun ScreenContainer(
    title: String,
    modifier: Modifier = Modifier,
    hasBottomBar: Boolean = false,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(horizontal = Spacing.large)
            .padding(
                top = Spacing.medium,
                bottom = if (hasBottomBar) Sizes.bottomBarClearance else Spacing.large,
            ),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            if (onBack != null) {
                GlassIconButton(
                    icon = R.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack,
                )
            }
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
        }
        content()
    }
}

@Preview(widthDp = 360, heightDp = 320)
@Composable
private fun ScreenContainerPreview() {
    ShotClockTheme {
        GlassBackground {
            ScreenContainer(title = stringResource(R.string.camera_title), onBack = {}) {
                GlassCard {
                    Text(
                        text = stringResource(R.string.camera_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
