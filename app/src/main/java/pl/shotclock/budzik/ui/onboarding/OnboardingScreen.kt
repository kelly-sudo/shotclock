package pl.shotclock.budzik.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.objects.ObjectImage
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance

/** Zaślepka onboardingu. Karty z wyjaśnieniem i prośby o uprawnienia powstaną w Etapie 7. */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.large, Alignment.CenterVertically),
    ) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .entranceFrom(rememberEntrance(order = 0)),
            tint = MintTint,
            shape = AppShapes.heroCard,
        ) {
            ObjectImage(
                visual = ObjectVisual.Toothbrush,
                contentDescription = null,
                modifier = Modifier.size(Sizes.illustrationCard),
            )
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.onboarding_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MintTint.secondaryText,
            )
        }
        AppButton(
            text = stringResource(R.string.onboarding_next),
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .entranceFrom(rememberEntrance(order = 1)),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingScreenPreview() {
    ShotClockTheme {
        AppBackground {
            OnboardingScreen(onFinish = {})
        }
    }
}
