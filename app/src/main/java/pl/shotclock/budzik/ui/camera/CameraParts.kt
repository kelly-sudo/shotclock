package pl.shotclock.budzik.ui.camera

import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.alarm.DefaultConfidenceThreshold
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ControlOffColor
import pl.shotclock.budzik.ui.theme.OnAccent
import pl.shotclock.budzik.ui.theme.SectionLabelText
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.ViewfinderColor
import pl.shotclock.budzik.ui.theme.rememberPressScale
import pl.shotclock.budzik.ui.theme.rememberPulse
import pl.shotclock.budzik.ui.theme.scaledBy
import kotlin.math.roundToInt

private const val ScanMillis = 1600
private const val ColorChangeMillis = 300

/**
 * Celownik: cztery narożniki i linia skanowania, która jeździ góra–dół.
 * Po rozpoznaniu narożniki zmieniają kolor na akcent, linia znika, a w środku pojawia się znaczek sukcesu.
 */
@Composable
fun Reticle(recognized: Boolean, modifier: Modifier = Modifier) {
    val scan = rememberPulse(ScanMillis, label = "scan line")
    val cornerColor by animateColorAsState(
        targetValue = if (recognized) Accent else ViewfinderColor,
        animationSpec = tween(ColorChangeMillis),
        label = "reticle color",
    )
    val lineAlpha by animateFloatAsState(if (recognized) 0f else 1f, tween(ColorChangeMillis), label = "scan alpha")

    Box(
        modifier = modifier
            // Własna warstwa: w każdej klatce rysuje się tylko celownik, a nie cały ekran.
            .graphicsLayer { }
            .drawBehind {
                val stroke = Sizes.reticleStroke.toPx()
                val corner = Sizes.reticleCorner.toPx()
                drawCorners(cornerColor, corner, stroke)
                // Linię czytamy dopiero przy rysowaniu, więc jej ruch nie przebudowuje ekranu.
                val inset = corner / 2
                val y = inset + (size.height - 2 * inset) * scan.value
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Accent, Color.Transparent),
                        startX = inset,
                        endX = size.width - inset,
                    ),
                    topLeft = Offset(inset, y),
                    size = Size(size.width - 2 * inset, Sizes.scanLine.toPx()),
                    alpha = lineAlpha,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        SuccessBadge(visible = recognized)
    }
}

/** Rysuje cztery zaokrąglone narożniki ramki. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCorners(color: Color, length: Float, stroke: Float) {
    val half = stroke / 2
    val right = size.width - half
    val bottom = size.height - half
    val path = Path().apply {
        // Lewy górny
        moveTo(half, length); lineTo(half, half); lineTo(length, half)
        // Prawy górny
        moveTo(right - length + half, half); lineTo(right, half); lineTo(right, length)
        // Prawy dolny
        moveTo(right, bottom - length + half); lineTo(right, bottom); lineTo(right - length + half, bottom)
        // Lewy dolny
        moveTo(length, bottom); lineTo(half, bottom); lineTo(half, bottom - length + half)
    }
    drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

/** Okrągły znaczek z „ptaszkiem”, który sprężyście wyskakuje po rozpoznaniu przedmiotu. */
@Composable
private fun SuccessBadge(visible: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "success badge",
    )
    Box(
        modifier = Modifier
            .size(Sizes.successBadge)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = scale.coerceIn(0f, 1f)
            }
            .clip(AppShapes.pill)
            .background(Accent),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = if (visible) stringResource(R.string.camera_recognized_badge) else null,
            tint = OnAccent,
            modifier = Modifier.size(Sizes.successIcon),
        )
    }
}

/**
 * Pasek pewności z podpisem w procentach. Pionowa kreska pokazuje próg, który trzeba przekroczyć.
 * Pasek jest w kolorze akcentu dopiero nad progiem.
 */
@Composable
fun ConfidenceBar(
    confidence: Float,
    modifier: Modifier = Modifier,
    threshold: Float = DefaultConfidenceThreshold,
) {
    val fill = animateFloatAsState(confidence.coerceIn(0f, 1f), tween(AnalysisAnimationMillis), label = "confidence")
    val aboveThreshold = confidence >= threshold
    val barColor by animateColorAsState(
        targetValue = if (aboveThreshold) Accent else TextSecondary,
        animationSpec = tween(ColorChangeMillis),
        label = "confidence color",
    )

    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.camera_confidence), style = SectionLabelText, color = TextSecondary)
            Text(
                text = stringResource(R.string.camera_confidence_value, (confidence * 100).roundToInt()),
                style = SectionLabelText,
                color = if (aboveThreshold) Accent else TextPrimary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.confidenceBar)
                .clip(AppShapes.pill)
                .background(ControlOffColor)
                .drawBehind {
                    val radius = CornerRadius(size.height / 2)
                    drawRoundRect(barColor, size = Size(size.width * fill.value, size.height), cornerRadius = radius)
                    val markX = size.width * threshold
                    drawRect(
                        color = TextPrimary,
                        topLeft = Offset(markX, 0f),
                        size = Size(Sizes.thresholdMark.toPx(), size.height),
                    )
                },
        )
    }
}

// Pasek dojeżdża do nowej wartości mniej więcej w czasie między dwiema analizowanymi klatkami.
private const val AnalysisAnimationMillis = 350

/** Przycisk migawki do ręcznego zdjęcia: białe kółko w białym pierścieniu, jak w aparacie. */
@Composable
fun ShutterButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    val description = stringResource(R.string.camera_take_photo)

    Box(
        modifier = modifier
            .scaledBy(scale)
            .size(Sizes.shutter)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .border(Sizes.shutterRing, ViewfinderColor, AppShapes.pill)
            .clip(AppShapes.pill)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            // Przycisk nie ma tekstu, więc czytnik ekranu potrzebuje opisu.
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(Sizes.shutterInner)
                .clip(AppShapes.pill)
                .background(ViewfinderColor),
        )
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
private fun ReticlePreview() {
    ShotClockTheme {
        AppBackground {
            Reticle(recognized = false, modifier = Modifier.padding(Spacing.large).size(Sizes.reticle))
        }
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
private fun ReticleRecognizedPreview() {
    ShotClockTheme {
        AppBackground {
            Reticle(recognized = true, modifier = Modifier.padding(Spacing.large).size(Sizes.reticle))
        }
    }
}

@Preview(widthDp = 360, heightDp = 120)
@Composable
private fun ConfidenceBarPreview() {
    ShotClockTheme {
        AppBackground {
            Column(Modifier.padding(Spacing.medium), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                ConfidenceBar(confidence = 0.62f)
                ConfidenceBar(confidence = 0.93f)
            }
        }
    }
}

@Preview(widthDp = 120, heightDp = 120)
@Composable
private fun ShutterButtonPreview() {
    ShotClockTheme {
        AppBackground {
            ShutterButton(onClick = {}, enabled = true, modifier = Modifier.padding(Spacing.medium))
        }
    }
}
