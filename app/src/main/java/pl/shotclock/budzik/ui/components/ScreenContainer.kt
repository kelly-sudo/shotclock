package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
 * [pinnedBottom] to element przypięty na dole, poza przewijaną treścią (np. „Zapisz alarm”).
 */
@Composable
fun ScreenContainer(
    title: String,
    modifier: Modifier = Modifier,
    hasBottomBar: Boolean = false,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
    pinnedBottom: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Odstęp od paska stanu jest poza przewijaniem, więc treść nie wjeżdża pod zegar i ikony telefonu.
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                // Gdy na dole nic nie jest przypięte, treść musi kończyć się nad paskiem nawigacji telefonu.
                .then(if (pinnedBottom == null) Modifier.navigationBarsPadding() else Modifier)
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
        if (pinnedBottom != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.screenEdge, vertical = Spacing.smallMedium),
            ) {
                pinnedBottom()
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 320)
@Composable
private fun ScreenContainerPreview() {
    ShotClockTheme {
        AppBackground {
            ScreenContainer(title = stringResource(R.string.tab_settings), onBack = {}) {
                AppCard {
                    Text(
                        text = stringResource(R.string.objects_description),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
