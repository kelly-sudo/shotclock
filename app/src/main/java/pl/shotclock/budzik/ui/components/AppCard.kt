package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.CardTint
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.NeutralTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.cardSurface

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    tint: CardTint = NeutralTint,
    shape: Shape = AppShapes.card,
    contentPadding: PaddingValues = PaddingValues(Spacing.screenEdge),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .cardSurface(tint, shape)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
        content = content,
    )
}

@Preview(widthDp = 360, heightDp = 170)
@Composable
private fun AppCardPreview() {
    ShotClockTheme {
        AppBackground {
            AppCard(Modifier.padding(Spacing.medium), tint = MintTint, shape = AppShapes.heroCard) {
                Text(
                    text = stringResource(R.string.onboarding_description),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
