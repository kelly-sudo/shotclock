package pl.shotclock.budzik.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ControlBorder
import pl.shotclock.budzik.ui.theme.HighlightColor
import pl.shotclock.budzik.ui.theme.OnHighlight
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.SurfaceColor
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.rememberPressScale
import pl.shotclock.budzik.ui.theme.scaledBy

/** Chip dnia tygodnia. [contentDescription] to pełna nazwa dnia dla czytników ekranu. */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    val surface by animateColorAsState(if (selected) HighlightColor else SurfaceColor, label = "chip surface")
    val border by animateColorAsState(if (selected) HighlightColor else ControlBorder, label = "chip border")
    val textColor by animateColorAsState(if (selected) OnHighlight else TextPrimary, label = "chip text")

    Box(
        modifier = modifier
            .scaledBy(scale)
            .size(Sizes.minTouchTarget)
            .clip(AppShapes.chip)
            .background(surface)
            .border(Sizes.cardBorder, border, AppShapes.chip)
            .toggleable(
                value = selected,
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Checkbox,
                onValueChange = onSelectedChange,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

@Preview(widthDp = 180, heightDp = 90)
@Composable
private fun AppChipPreview() {
    val shortNames = stringArrayResource(R.array.weekdays_short)
    val fullNames = stringArrayResource(R.array.weekdays_full)
    ShotClockTheme {
        AppBackground {
            Row(
                modifier = Modifier.padding(Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                AppChip(shortNames[0], selected = true, onSelectedChange = {}, contentDescription = fullNames[0])
                AppChip(shortNames[1], selected = false, onSelectedChange = {}, contentDescription = fullNames[1])
            }
        }
    }
}
