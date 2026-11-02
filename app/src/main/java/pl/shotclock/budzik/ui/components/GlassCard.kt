package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.glassSurface

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .glassSurface(AppShapes.card)
            .padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        content = content,
    )
}

@Preview(widthDp = 360, heightDp = 200)
@Composable
private fun GlassCardPreview() {
    ShotClockTheme {
        GlassBackground {
            GlassCard(Modifier.padding(Spacing.medium)) {
                Text(
                    text = stringResource(R.string.onboarding_description),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
