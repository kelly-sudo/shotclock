package pl.shotclock.budzik.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

private const val PressedScale = 0.95f

private val GlassSheenBrush = Brush.verticalGradient(0f to GlassSheen, 0.5f to Color.Transparent)

/**
 * Wygląd „szkła”: półprzezroczyste wypełnienie, połysk u góry, cienka jasna krawędź i miękki cień.
 *
 * Na razie to styl bez rozmycia, działający tak samo na każdej wersji Androida.
 * Rozmycie dla API 31+ (Etap 7) dojdzie właśnie tutaj, więc ekrany i komponenty się nie zmienią.
 */
fun Modifier.glassSurface(shape: Shape): Modifier = this
    .dropShadow(
        shape = shape,
        shadow = Shadow(
            radius = Sizes.shadowRadius,
            color = GlassShadow,
            offset = DpOffset(0.dp, Sizes.shadowOffset),
        ),
    )
    .clip(shape)
    .background(GlassFill)
    .background(GlassSheenBrush)
    .border(Sizes.glassBorder, GlassBorder, shape)

fun Modifier.accentSurface(shape: Shape): Modifier = this
    .clip(shape)
    .background(Accent)

/** Skala przycisku: lekko maleje przy naciśnięciu i sprężyście wraca. */
@Composable
fun rememberPressScale(interactionSource: InteractionSource): State<Float> {
    val isPressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (isPressed) PressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press scale",
    )
}

// Skalę czytamy wewnątrz graphicsLayer, żeby animacja nie przebudowywała przycisku w każdej klatce.
fun Modifier.scaledBy(scale: State<Float>): Modifier = graphicsLayer {
    scaleX = scale.value
    scaleY = scale.value
}
