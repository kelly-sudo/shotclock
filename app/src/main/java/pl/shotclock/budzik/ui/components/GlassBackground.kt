package pl.shotclock.budzik.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.ui.theme.BackgroundGradient
import pl.shotclock.budzik.ui.theme.ShotClockTheme

private const val ShiftDurationMillis = 8000

// O jaką część wysokości ekranu przesuwa się gradient podczas animacji.
private const val MaxShift = 0.3f

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val shift = if (animated) {
        rememberInfiniteTransition(label = "background").animateFloat(
            initialValue = 0f,
            targetValue = MaxShift,
            animationSpec = infiniteRepeatable(
                animation = tween(ShiftDurationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "background shift",
        )
    } else {
        rememberUpdatedState(0f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Dwa zabiegi, żeby animacja nie spowalniała starszego Samsunga:
        // - przesunięcie czytamy dopiero przy rysowaniu, więc ekran nie jest przebudowywany w każdej klatce,
        // - tło ma własną warstwę (graphicsLayer), więc karty i cienie na nim nie są rysowane od nowa.
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { }
                .drawBehind {
                    val offsetY = size.height * shift.value
                    drawRect(
                        Brush.linearGradient(
                            colors = BackgroundGradient,
                            start = Offset(0f, -offsetY),
                            end = Offset(size.width, size.height - offsetY),
                        ),
                    )
                },
        )
        content()
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun GlassBackgroundPreview() {
    ShotClockTheme {
        GlassBackground {}
    }
}
