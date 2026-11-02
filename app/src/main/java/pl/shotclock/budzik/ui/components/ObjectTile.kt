package pl.shotclock.budzik.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.ui.objects.ObjectImage
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.rememberPressScale
import pl.shotclock.budzik.ui.theme.scaledBy

/** Kafelek wyboru przedmiotu w kolorze tego przedmiotu. */
@Composable
fun ObjectTile(
    visual: ObjectVisual,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    val border by animateColorAsState(if (selected) Accent else Color.Transparent, label = "tile border")

    Column(
        modifier = modifier
            .scaledBy(scale)
            .height(Sizes.objectTileHeight)
            .clip(AppShapes.tile)
            .background(visual.tint.surface)
            .border(Sizes.selectedBorder, border, AppShapes.tile)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(Spacing.extraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall, Alignment.CenterVertically),
    ) {
        // Opis nie jest potrzebny, bo pod ilustracją jest nazwa.
        ObjectImage(visual, contentDescription = null, modifier = Modifier.size(Sizes.illustrationTile))
        Text(
            text = stringResource(visual.shortNameRes),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Preview(widthDp = 120, heightDp = 110)
@Composable
private fun ObjectTilePreview() {
    ShotClockTheme {
        AppBackground {
            ObjectTile(ObjectVisual.Toothbrush, selected = true, onClick = {})
        }
    }
}
