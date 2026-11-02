package pl.shotclock.budzik.ui.objects

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.theme.CardTint
import pl.shotclock.budzik.ui.theme.LilacTint
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.PeachTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme

/**
 * Wygląd przedmiotu w jednym miejscu: nazwa, ilustracja i kolor karty.
 * Gdy zespół AI wybierze drugi przedmiot, wystarczy zmienić tutaj [Mug].
 */
enum class ObjectVisual(
    @param:StringRes val nameRes: Int,
    @param:StringRes val shortNameRes: Int,
    @param:DrawableRes val imageRes: Int,
    val tint: CardTint,
    // Ilustracje mają własne kolory. Tylko zwykła ikona, jak kostka, dostaje kolor z kodu.
    val imageColor: Color? = null,
) {
    Toothbrush(
        nameRes = R.string.object_toothbrush,
        shortNameRes = R.string.object_toothbrush_short,
        imageRes = R.drawable.ill_toothbrush,
        tint = MintTint,
    ),
    Mug(
        nameRes = R.string.object_mug,
        shortNameRes = R.string.object_mug,
        imageRes = R.drawable.ill_mug,
        tint = PeachTint,
    ),
    Random(
        nameRes = R.string.object_random_description,
        shortNameRes = R.string.object_random,
        imageRes = R.drawable.ic_dice,
        tint = LilacTint,
        imageColor = LilacTint.secondaryText,
    ),
}

@Composable
fun ObjectImage(
    visual: ObjectVisual,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(visual.imageRes),
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = visual.imageColor?.let { ColorFilter.tint(it) },
    )
}

@Preview(widthDp = 200, heightDp = 200)
@Composable
private fun ObjectImagePreview() {
    ShotClockTheme {
        AppBackground {
            ObjectImage(ObjectVisual.Toothbrush, contentDescription = null)
        }
    }
}
