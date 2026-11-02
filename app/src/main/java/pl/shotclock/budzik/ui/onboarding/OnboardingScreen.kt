package pl.shotclock.budzik.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.GlassBackground
import pl.shotclock.budzik.ui.components.GlassButton
import pl.shotclock.budzik.ui.components.GlassCard
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Spacing

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
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.onboarding_description),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        GlassButton(
            text = stringResource(R.string.onboarding_next),
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun OnboardingScreenPreview() {
    ShotClockTheme {
        GlassBackground {
            OnboardingScreen(onFinish = {})
        }
    }
}
