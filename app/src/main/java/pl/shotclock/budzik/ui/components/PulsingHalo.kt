package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AccentGlow
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.rememberLoop
import pl.shotclock.budzik.ui.theme.rememberPulse

private const val WaveMillis = 2600
private const val PulseMillis = 1500
private const val WaveCount = 3

/** Pulsująca poświata i rozchodzące się fale wokół przedmiotu na ekranie dzwonienia. */
@Composable
fun PulsingHalo(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val wave = rememberLoop(WaveMillis, label = "halo waves")
    val pulse = rememberPulse(PulseMillis, label = "halo glow")

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Własna warstwa (graphicsLayer) sprawia, że w każdej klatce rysują się tylko fale, a nie reszta ekranu.
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { }
                .drawBehind {
                    val baseRadius = size.minDimension / 2
                    val glowRadius = baseRadius * (1.3f + 0.2f * pulse.value)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AccentGlow, Color.Transparent),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = glowRadius,
                        ),
                        radius = glowRadius,
                        alpha = 0.55f + 0.45f * pulse.value,
                    )
                    repeat(WaveCount) { index ->
                        val progress = (wave.value + index.toFloat() / WaveCount) % 1f
                        drawCircle(
                            color = Accent,
                            // Fale kończą się na krawędzi pola, żeby nie nachodziły na sąsiednie teksty.
                            radius = baseRadius * (0.68f + 0.32f * progress),
                            alpha = 0.8f * (1f - progress),
                            style = Stroke(width = Sizes.selectedBorder.toPx()),
                        )
                    }
                },
        )
        content()
    }
}

@Preview(widthDp = 320, heightDp = 320)
@Composable
private fun PulsingHaloPreview() {
    ShotClockTheme {
        AppBackground {
            PulsingHalo(Modifier.align(Alignment.Center)) {}
        }
    }
}
