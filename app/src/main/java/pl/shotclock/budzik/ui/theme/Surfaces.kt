package pl.shotclock.budzik.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape

/** Karta: pełne tło w kolorze z [tint] i cienka krawędź. Bez przezroczystości, więc wygląda tak samo na każdym Androidzie. */
fun Modifier.cardSurface(tint: CardTint = NeutralTint, shape: Shape = AppShapes.card): Modifier = this
    .clip(shape)
    .background(tint.surface)
    .border(Sizes.cardBorder, tint.border, shape)

fun Modifier.accentSurface(shape: Shape): Modifier = this
    .clip(shape)
    .background(Accent)
