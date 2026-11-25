package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.TimeTextLarge
import pl.shotclock.budzik.ui.theme.cardSurface

private const val HoursInDay = 24
private const val MinutesInHour = 60

// Wiersz jest niższy niż duża cyfra: wybrana wartość może na niego wystawać, bo sąsiednie są pomniejszone.
private const val RowHeightRatio = 0.62f
private const val NeighbourScale = 0.4f
private const val NeighbourAlpha = 0.5f

/**
 * Przewijany wybór godziny: dwie kolumny (godziny i minuty). Wybrana jest wartość w środku.
 * Kolumnę można przewinąć palcem albo stuknąć wartość nad lub pod środkiem.
 */
@Composable
fun TimeWheel(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cyfry są i tak bardzo duże, więc nie rosną razem z czcionką systemową. Inaczej nie zmieściłyby się na wąskim ekranie.
    val fontScale = LocalDensity.current.fontScale
    val digitStyle = TimeTextLarge.copy(
        fontSize = TimeTextLarge.fontSize / fontScale,
        lineHeight = TimeTextLarge.lineHeight / fontScale,
    )
    val rowHeight = with(LocalDensity.current) { digitStyle.lineHeight.toDp() } * RowHeightRatio
    val columnWidth = rememberDigitsWidth(digitStyle)
    // Aktualne wartości czytamy przez rememberUpdatedState, bo kolumna wywołuje te funkcje po zakończeniu przewijania.
    val currentHour by rememberUpdatedState(hour)
    val currentMinute by rememberUpdatedState(minute)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WheelColumn(
            count = HoursInDay,
            selected = hour,
            onSelectedChange = { onTimeChange(it, currentMinute) },
            label = stringResource(R.string.time_wheel_hours),
            digitStyle = digitStyle,
            rowHeight = rowHeight,
            width = columnWidth,
        )
        Text(text = ":", style = digitStyle, color = Accent)
        WheelColumn(
            count = MinutesInHour,
            selected = minute,
            onSelectedChange = { onTimeChange(currentHour, it) },
            label = stringResource(R.string.time_wheel_minutes),
            digitStyle = digitStyle,
            rowHeight = rowHeight,
            width = columnWidth,
        )
    }
}

/**
 * Jedna kolumna wartości od 0 do [count] - 1. Na początku i na końcu listy jest pusty wiersz,
 * więc gdy przewijanie się zatrzyma, wybrana wartość jest zawsze równa numerowi pierwszego widocznego wiersza.
 */
@Composable
private fun WheelColumn(
    count: Int,
    selected: Int,
    onSelectedChange: (Int) -> Unit,
    label: String,
    digitStyle: TextStyle,
    rowHeight: Dp,
    width: Dp,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val scope = rememberCoroutineScope()
    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelectedChange by rememberUpdatedState(onSelectedChange)
    val rowHeightPx = with(LocalDensity.current) { rowHeight.toPx() }

    // Po zatrzymaniu przewijania (wiersz jest już przyciągnięty na miejsce) zgłaszamy nową wartość.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { inProgress -> !inProgress }
            .collect {
                if (listState.firstVisibleItemIndex != currentSelected) {
                    currentOnSelectedChange(listState.firstVisibleItemIndex)
                }
            }
    }
    // Gdy wartość zmieni się z zewnątrz (np. wczyta się z bazy), przewijamy do niej.
    LaunchedEffect(selected) {
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != selected) {
            listState.scrollToItem(selected)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = rememberSnapFlingBehavior(listState, snapPosition = SnapPosition.Start),
        modifier = Modifier
            .width(width)
            .height(rowHeight * 3)
            .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { Box(Modifier.height(rowHeight)) }
        items(count) { value ->
            Box(
                modifier = Modifier
                    .height(rowHeight)
                    .fillMaxWidth()
                    // Stuknięcie wartości przewija do niej. Bez efektu fali, bo cyfry nie wyglądają jak przycisk.
                    .clickable(interactionSource = null, indication = null) {
                        scope.launch { listState.animateScrollToItem(value) }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = String.format(Locale.ROOT, "%02d", value),
                    style = digitStyle,
                    color = TextPrimary,
                    modifier = Modifier
                        .wrapContentHeight(unbounded = true)
                        // Skalę liczymy w graphicsLayer, więc przy przewijaniu zmienia się tylko rysowanie, bez przebudowy ekranu.
                        .graphicsLayer {
                            val distance = distanceFromCenter(listState, listIndex = value + 1, rowHeightPx)
                            val scale = 1f - distance * (1f - NeighbourScale)
                            scaleX = scale
                            scaleY = scale
                            alpha = 1f - distance * (1f - NeighbourAlpha)
                        },
                )
            }
        }
        item { Box(Modifier.height(rowHeight)) }
    }
}

/** Jak daleko wiersz jest od środka kolumny: 0 w samym środku, 1 o jeden wiersz dalej lub więcej. */
private fun distanceFromCenter(listState: LazyListState, listIndex: Int, rowHeightPx: Float): Float {
    // Środkowe miejsce zajmuje wiersz o numerze firstVisibleItemIndex + 1.
    val rowsFromCenter = listIndex - listState.firstVisibleItemIndex - 1 -
        listState.firstVisibleItemScrollOffset / rowHeightPx
    return abs(rowsFromCenter).coerceAtMost(1f)
}

/** Szerokość dwóch cyfr w danym stylu, żeby kolumna nie ucinała liczb. */
@Composable
private fun rememberDigitsWidth(style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(style, density) {
        with(density) { textMeasurer.measure("00", style).size.width.toDp() }
    }
}

@Preview(widthDp = 360, heightDp = 220)
@Composable
private fun TimeWheelPreview() {
    ShotClockTheme {
        AppBackground {
            TimeWheel(
                hour = 7,
                minute = 30,
                onTimeChange = { _, _ -> },
                modifier = Modifier
                    .padding(Spacing.medium)
                    .fillMaxWidth()
                    .cardSurface(MintTint),
            )
        }
    }
}
