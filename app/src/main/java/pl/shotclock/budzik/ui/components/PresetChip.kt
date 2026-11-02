package pl.shotclock.budzik.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ControlBorder
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.SurfaceColor
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.rememberPressScale
import pl.shotclock.budzik.ui.theme.scaledBy

/** Szybki wybór, np. „Dni robocze”. Niższy niż 48 dp, więc pole dotyku ma sztucznie powiększone marginesem. */
@Composable
fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    val surface by animateColorAsState(if (selected) MintTint.surface else SurfaceColor, label = "preset surface")
    val border by animateColorAsState(if (selected) Accent else ControlBorder, label = "preset border")
    val textColor by animateColorAsState(if (selected) Accent else TextPrimary, label = "preset text")

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = Sizes.minTouchTarget)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .scaledBy(scale)
                .height(Sizes.presetChipHeight)
                .clip(AppShapes.pill)
                .background(surface)
                .border(Sizes.cardBorder, border, AppShapes.pill)
                .padding(horizontal = Spacing.medium),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor)
        }
    }
}

@Preview(widthDp = 200, heightDp = 90)
@Composable
private fun PresetChipPreview() {
    ShotClockTheme {
        AppBackground {
            PresetChip(
                label = stringResource(R.string.edit_preset_workdays),
                selected = true,
                onClick = {},
                modifier = Modifier.padding(Spacing.medium),
            )
        }
    }
}
