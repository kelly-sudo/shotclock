package pl.shotclock.budzik.ui.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.rememberPressScale
import pl.shotclock.budzik.ui.theme.scaledBy

/** Okrągły chip dnia tygodnia. [contentDescription] to pełna nazwa dnia dla czytników ekranu. */
@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    val style = if (selected) GlassButtonStyle.Primary else GlassButtonStyle.Secondary

    Box(
        modifier = modifier
            .scaledBy(scale)
            .size(Sizes.minTouchTarget)
            .buttonSurface(style, AppShapes.pill)
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = buttonContentColor(style),
        )
    }
}

@Preview(widthDp = 200, heightDp = 100)
@Composable
private fun GlassChipPreview() {
    val shortNames = stringArrayResource(R.array.weekdays_short)
    val fullNames = stringArrayResource(R.array.weekdays_full)
    ShotClockTheme {
        GlassBackground {
            Row(
                modifier = Modifier.padding(Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                GlassChip(shortNames[0], selected = true, onSelectedChange = {}, contentDescription = fullNames[0])
                GlassChip(shortNames[1], selected = false, onSelectedChange = {}, contentDescription = fullNames[1])
            }
        }
    }
}
