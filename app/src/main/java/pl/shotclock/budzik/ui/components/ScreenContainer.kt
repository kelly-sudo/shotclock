package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
 * z odstępami od pasków systemowych. Ekran z cofaniem ma mniejszy tytuł, bo obok jest przycisk.
 */
@Composable
fun ScreenContainer(
    title: String,
    modifier: Modifier = Modifier,
    hasBottomBar: Boolean = false,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(horizontal = Spacing.screenEdge)
            .padding(
                top = Spacing.medium,
                bottom = if (hasBottomBar) Sizes.bottomBarClearance else Spacing.large,
            ),
        verticalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
        ) {
            if (onBack != null) {
                AppIconButton(
                    icon = R.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack,
                )
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = if (onBack != null) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineLarge
                },
            )
            trailing()
        }
        content()
    }
}

@Preview(widthDp = 360, heightDp = 320)
@Composable
private fun ScreenContainerPreview() {
    ShotClockTheme {
        AppBackground {
            ScreenContainer(title = stringResource(R.string.camera_title), onBack = {}) {
                AppCard {
                    Text(
                        text = stringResource(R.string.camera_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
