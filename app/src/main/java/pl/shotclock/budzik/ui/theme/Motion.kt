package pl.shotclock.budzik.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Wszystkie animacje czytają swoje wartości wewnątrz graphicsLayer. Dzięki temu w każdej klatce zmienia się
// tylko warstwa elementu, a ekran nie jest przebudowywany, co jest ważne na starszym Samsungu.

const val ScreenTransitionMillis = 200

private const val PressedScale = 0.94f
private const val EntranceMillis = 600
private const val EntranceStepMillis = 90L
private const val ShakeLoopMillis = 2600
private val EntranceOffset = 20.dp

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

fun Modifier.scaledBy(scale: State<Float>): Modifier = graphicsLayer {
    scaleX = scale.value
    scaleY = scale.value
}

/**
 * Postęp wjazdu elementu po otwarciu ekranu, od 0 do 1. Elementy z kolejnym [order] startują nieco później.
 * W podglądzie @Preview animacje nie działają, więc tam od razu zwracamy 1, żeby element był widoczny.
 */
@Composable
fun rememberEntrance(order: Int = 0): State<Float> {
    val isPreview = LocalInspectionMode.current
    val progress = remember { Animatable(if (isPreview) 1f else 0f) }
    LaunchedEffect(Unit) {
        delay(order * EntranceStepMillis)
        progress.animateTo(1f, tween(EntranceMillis, easing = FastOutSlowInEasing))
    }
    return progress.asState()
}

/** Element pojawia się i podjeżdża lekko od dołu. */
fun Modifier.entranceFrom(progress: State<Float>): Modifier = graphicsLayer {
    alpha = progress.value
    translationY = (1f - progress.value) * EntranceOffset.toPx()
}

/** Wartość od 0 do 1 i z powrotem, bez końca: do pulsowania, migania i unoszenia się. */
@Composable
fun rememberPulse(durationMillis: Int, label: String): State<Float> =
    rememberInfiniteTransition(label = label).animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = label,
    )

/** Wartość rosnąca od 0 do 1 i zaczynająca od nowa: do rozchodzących się fal. */
@Composable
fun rememberLoop(durationMillis: Int, label: String): State<Float> =
    rememberInfiniteTransition(label = label).animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing)),
        label = label,
    )

/** Kąt obrotu dzwoniącego przedmiotu: przez chwilę stoi, potem krótko drży. */
@Composable
fun rememberShakeAngle(): State<Float> =
    rememberInfiniteTransition(label = "shake").animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = ShakeLoopMillis
                0f at 1350
                -9f at 1456
                8f at 1560
                -7f at 1664
                6f at 1768
                -3f at 1872
                0f at 1976
            },
        ),
        label = "shake angle",
    )
