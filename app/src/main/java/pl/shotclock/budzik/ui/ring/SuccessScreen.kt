package pl.shotclock.budzik.ui.ring

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import pl.shotclock.budzik.R
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.data.WakeEvent
import pl.shotclock.budzik.data.WakeSummary
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.formatDuration
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AccentGlow
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.CelebrationAmber
import pl.shotclock.budzik.ui.theme.ControlOffColor
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.OnAccent
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.StatValueMinSize
import pl.shotclock.budzik.ui.theme.SuccessTitleText
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.TimeTextMedium
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.rememberEntrance

// Cała animacja ptaszka trwa tyle. Niżej jej fazy jako ułamki tego czasu (od 0 do 1).
private const val CheckAnimationMillis = 2_000
private const val CircleStart = 0.05f
private const val CircleEnd = 0.45f
private const val TickStart = 0.4f
private const val TickEnd = 0.65f
private const val BurstStart = 0.45f
private const val BurstEnd = 0.95f
// Każda fala trwa tyle, a druga startuje nieco po pierwszej. Obie kończą się przed końcem animacji.
private const val RippleLength = 0.4f
private const val SecondRippleDelay = 0.12f
private const val SparkCount = 9

// Ptaszek na makiecie leży w kwadracie 160 × 160: kółko o promieniu 70 i trzy punkty ptaszka.
private const val DesignBox = 160f
private const val CircleRadius = 70f
private val TickPoints = listOf(Offset(50f, 83f), Offset(71f, 104f), Offset(111f, 58f))

// Wjazd tekstów i kart zaczyna się, gdy ptaszek jest już narysowany.
private const val FirstEntranceOrder = 6

/**
 * Ekran sukcesu po wyłączeniu alarmu, według makiety „Sukces”: rysowany ptaszek z falami i iskrami,
 * czas wstania, seria dni, kółka z dniami tygodnia i przycisk „Gotowe”.
 */
@Composable
fun SuccessScreen(
    summary: WakeSummary,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Spacing.extraLarge))
        // Na niskim ekranie ptaszek się zmniejsza, żeby karty i przycisk zawsze się mieściły.
        SuccessCheck(
            Modifier
                .weight(1f, fill = false)
                .sizeIn(maxWidth = Sizes.successCheck, maxHeight = Sizes.successCheck)
                .aspectRatio(1f, matchHeightConstraintsFirst = true),
        )
        Column(
            modifier = Modifier
                .padding(top = Spacing.large)
                .entranceFrom(rememberEntrance(order = FirstEntranceOrder)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        ) {
            Text(stringResource(R.string.success_title), style = SuccessTitleText, textAlign = TextAlign.Center)
            Text(
                text = subtitle(summary.event),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        StatCards(
            summary = summary,
            modifier = Modifier
                .padding(top = Spacing.large)
                .entranceFrom(rememberEntrance(order = FirstEntranceOrder + 1)),
        )
        WeekCard(
            summary = summary,
            modifier = Modifier
                .padding(top = Spacing.smallMedium)
                .entranceFrom(rememberEntrance(order = FirstEntranceOrder + 2)),
        )
        Spacer(Modifier.weight(1f))
        AppButton(
            text = stringResource(R.string.success_done),
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.medium)
                .entranceFrom(rememberEntrance(order = FirstEntranceOrder + 3)),
        )
    }
}

/** „Rozpoznano: Szczoteczka, pewność 96%” albo „Wyłączony zadaniem matematycznym”. */
@Composable
private fun subtitle(event: WakeEvent): String {
    val recognized = event.recognizedObject
    val confidence = event.confidence
    return if (event.emergency || recognized == null || confidence == null) {
        stringResource(R.string.success_emergency)
    } else {
        stringResource(
            R.string.success_recognized,
            stringResource(ObjectVisual.of(recognized).shortNameRes),
            (confidence * 100).roundToInt(),
        )
    }
}

/** Dwie karty obok siebie: czas wstania i seria dni z płomieniem. */
@Composable
private fun StatCards(summary: WakeSummary, modifier: Modifier = Modifier) {
    // IntrinsicSize.Min: obie karty mają wysokość wyższej z nich.
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        StatCard(
            label = stringResource(R.string.success_wake_time),
            modifier = Modifier.weight(1f).fillMaxHeight(),
        ) {
            val valueStyle = MaterialTheme.typography.titleLarge
            // Na wąskim ekranie „10 min 42 s” zmniejsza się, zamiast przejść do drugiej linii.
            Text(
                text = formatDuration(summary.wakeUpSeconds),
                style = valueStyle,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = StatValueMinSize, maxFontSize = valueStyle.fontSize),
            )
        }
        StatCard(
            label = pluralStringResource(R.plurals.success_streak_label, summary.streakDays),
            modifier = Modifier.weight(1f).fillMaxHeight(),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            ) {
                Image(
                    painter = painterResource(R.drawable.ill_flame),
                    contentDescription = null,
                    modifier = Modifier.size(Sizes.illustrationRow),
                )
                Text(summary.streakDays.toString(), style = TimeTextMedium)
            }
        }
    }
}

/** Karta z wartością na dole (wyrównaną w obu kartach) i podpisem pod spodem. */
@Composable
private fun StatCard(
    label: String,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit,
) {
    AppCard(modifier = modifier, verticalSpacing = Spacing.extraSmall) {
        Spacer(Modifier.weight(1f))
        value()
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

/** Siedem kółek od poniedziałku do niedzieli. Dzień z udaną pobudką jest w kolorze akcentu, dziś ma obwódkę. */
@Composable
private fun WeekCard(summary: WakeSummary, modifier: Modifier = Modifier) {
    val shortNames = stringArrayResource(R.array.weekdays_short)
    val fullNames = stringArrayResource(R.array.weekdays_full)
    val goodDays = DayOfWeek.entries.filter { it in summary.goodDaysThisWeek }
    // Czytnik ekranu nie „widzi” kolorów kółek, więc dostaje jedno zdanie z nazwami udanych dni.
    val description = if (goodDays.isEmpty()) {
        stringResource(R.string.success_week_description_none)
    } else {
        stringResource(
            R.string.success_week_description,
            goodDays.joinToString(", ") { fullNames[it.value - 1] },
        )
    }

    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = description },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DayOfWeek.entries.forEachIndexed { index, day ->
                WeekDayDot(
                    label = shortNames[day.value - 1],
                    good = day in summary.goodDaysThisWeek,
                    isToday = day == summary.today,
                    order = FirstEntranceOrder + 2 + index,
                )
            }
        }
    }
}

@Composable
private fun WeekDayDot(label: String, good: Boolean, isToday: Boolean, order: Int) {
    val pop = rememberEntrance(order)
    Box(
        modifier = Modifier
            .size(Sizes.weekDay)
            // Kółko „wyskakuje”: rośnie od 40% do pełnego rozmiaru.
            .graphicsLayer {
                val scale = 0.4f + 0.6f * pop.value
                scaleX = scale
                scaleY = scale
                alpha = pop.value
            }
            // Dziś ma poświatę dookoła kółka. Rysujemy ją poza kółkiem, więc nie zajmuje miejsca w rzędzie.
            .then(if (isToday) Modifier.drawBehind { drawTodayRing(Sizes.weekDayTodayRing.toPx()) } else Modifier)
            .clip(AppShapes.pill)
            .background(if (good) Accent else ControlOffColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (good) OnAccent else TextSecondary,
        )
    }
}

private fun DrawScope.drawTodayRing(width: Float) {
    drawCircle(color = AccentGlow, radius = size.minDimension / 2 + width / 2, style = Stroke(width))
}

/** Postęp animacji ptaszka od 0 do 1. W podglądzie @Preview od razu 1, czyli gotowy ptaszek. */
@Composable
private fun rememberCheckProgress(): State<Float> {
    val isPreview = LocalInspectionMode.current
    val progress = remember { Animatable(if (isPreview) 1f else 0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(CheckAnimationMillis, easing = LinearEasing))
    }
    return progress.asState()
}

/** Która część fazy [start]–[end] już minęła, od 0 do 1. */
private fun phase(progress: Float, start: Float, end: Float): Float =
    ((progress - start) / (end - start)).coerceIn(0f, 1f)

/**
 * Animowany ptaszek: najpierw rysuje się kółko, potem ptaszek, a na koniec rozchodzą się dwie fale i iskry.
 * Kształty liczymy raz (drawWithCache), a postęp czytamy dopiero przy rysowaniu, więc ekran nie jest przebudowywany.
 */
@Composable
private fun SuccessCheck(modifier: Modifier = Modifier) {
    val progress = rememberCheckProgress()
    val sparkColors = listOf(Accent, TextPrimary, CelebrationAmber)

    Box(
        modifier = modifier.drawWithCache {
            val scale = size.minDimension / DesignBox
            val center = Offset(size.width / 2, size.height / 2)
            val radius = CircleRadius * scale
            val circleStroke = Sizes.successCheckStroke.toPx()
            val tickStroke = Sizes.successTickStroke.toPx()
            val rippleStroke = 2.dp.toPx()
            val sparkRadius = Sizes.successSpark.toPx() / 2
            // Gdy ptaszek jest mniejszy (niski ekran), iskry też lecą proporcjonalnie bliżej.
            val sparkReach = Sizes.successSparkReach.toPx() * size.minDimension / Sizes.successCheck.toPx()
            val topLeft = Offset(center.x - size.minDimension / 2, center.y - size.minDimension / 2)
            val tick = Path().apply {
                TickPoints.forEachIndexed { index, point ->
                    val x = topLeft.x + point.x * scale
                    val y = topLeft.y + point.y * scale
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            val tickMeasure = PathMeasure().apply { setPath(tick, forceClosed = false) }
            val partialTick = Path()

            onDrawBehind {
                val t = progress.value

                // Dwie fale: rosną od 70% do 150% i znikają.
                listOf(0f, SecondRippleDelay).forEach { delay ->
                    val ripple = phase(t, BurstStart + delay, BurstStart + delay + RippleLength)
                    if (ripple > 0f && ripple < 1f) {
                        drawCircle(
                            color = Accent,
                            radius = size.minDimension / 2 * (0.7f + 0.8f * ripple),
                            center = center,
                            alpha = 0.9f * (1f - ripple),
                            style = Stroke(rippleStroke),
                        )
                    }
                }

                // Iskry wylatują na boki: szybko się pojawiają i powoli gasną.
                val burst = phase(t, BurstStart, BurstEnd)
                if (burst > 0f && burst < 1f) {
                    val distance = sparkReach * FastOutSlowInEasing.transform(burst)
                    val alpha = if (burst < 0.25f) burst / 0.25f else 1f - (burst - 0.25f) / 0.75f
                    repeat(SparkCount) { index ->
                        val angle = 2 * PI * index / SparkCount - PI / 2
                        drawCircle(
                            color = sparkColors[index % sparkColors.size],
                            radius = sparkRadius,
                            center = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * distance,
                            alpha = alpha,
                        )
                    }
                }

                // Kółko: tło w kolorze karty przedmiotu i obwód rysowany od góry zgodnie z ruchem wskazówek.
                val circle = FastOutSlowInEasing.transform(phase(t, CircleStart, CircleEnd))
                drawCircle(MintTint.surface, radius = radius, center = center, alpha = circle)
                drawArc(
                    color = Accent,
                    startAngle = -90f,
                    sweepAngle = 360f * circle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(circleStroke),
                )

                // Ptaszek rysuje się od lewej do prawej.
                val tickProgress = FastOutSlowInEasing.transform(phase(t, TickStart, TickEnd))
                if (tickProgress > 0f) {
                    partialTick.reset()
                    tickMeasure.getSegment(0f, tickMeasure.length * tickProgress, partialTick, startWithMoveTo = true)
                    drawPath(
                        path = partialTick,
                        color = Accent,
                        style = Stroke(width = tickStroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
        },
    )
}

/** Przykładowa pobudka do podglądów: dzwonił 1 min 42 s, szczoteczka rozpoznana z pewnością 96%. */
private fun sampleSummary(emergency: Boolean = false) = WakeSummary(
    event = WakeEvent(
        alarmId = 1,
        ringStartedAt = 0,
        dismissedAt = 102_000,
        attempts = if (emergency) 10 else 1,
        recognizedObject = if (emergency) null else RequiredObject.FIRST,
        confidence = if (emergency) null else 0.96f,
        emergency = emergency,
    ),
    wakeUpSeconds = 102,
    streakDays = if (emergency) 0 else 8,
    goodDaysThisWeek = if (emergency) {
        setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
    } else {
        setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
    },
    today = DayOfWeek.THURSDAY,
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SuccessScreenPreview() {
    ShotClockTheme {
        AppBackground {
            SuccessScreen(summary = sampleSummary(), onDone = {})
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun SuccessScreenEmergencyPreview() {
    ShotClockTheme {
        AppBackground {
            SuccessScreen(summary = sampleSummary(emergency = true), onDone = {})
        }
    }
}
