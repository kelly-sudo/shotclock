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

/** Półprzezroczysty ciemny panel nad podglądem kamery. Tylko na ekranie kamery, reszta aplikacji jest bez przezroczystości. */
fun Modifier.glassSurface(shape: Shape): Modifier = this
    .clip(shape)
    .background(GlassColor)
    .border(Sizes.cardBorder, GlassBorder, shape)
