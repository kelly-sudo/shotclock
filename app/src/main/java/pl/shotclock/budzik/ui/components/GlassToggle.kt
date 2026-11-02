package pl.shotclock.budzik.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.GlassBorder
import pl.shotclock.budzik.ui.theme.GlassFill
import pl.shotclock.budzik.ui.theme.OnAccent
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextPrimary

@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val thumbTravel = Sizes.toggleWidth - Sizes.toggleThumb - Sizes.togglePadding * 2
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) thumbTravel else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "toggle thumb",
    )
    val trackColor by animateColorAsState(if (checked) Accent else GlassFill, label = "toggle track")
    val thumbColor by animateColorAsState(if (checked) OnAccent else TextPrimary, label = "toggle color")

    // Zewnętrzny Box daje pole dotyku min. 48 dp, a widoczny tor przełącznika jest niższy.
    Box(
        modifier = modifier
            .clip(AppShapes.pill)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .semantics { this.contentDescription = contentDescription }
            .defaultMinSize(minWidth = Sizes.minTouchTarget, minHeight = Sizes.minTouchTarget),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.toggleWidth, Sizes.toggleHeight)
                .clip(AppShapes.pill)
                .background(trackColor)
                .border(Sizes.glassBorder, GlassBorder, AppShapes.pill)
                .padding(Sizes.togglePadding),
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffset.roundToPx(), 0) }
                    .size(Sizes.toggleThumb)
                    .clip(AppShapes.pill)
                    .background(thumbColor),
            )
        }
    }
}

@Preview(widthDp = 200, heightDp = 100)
@Composable
private fun GlassTogglePreview() {
    ShotClockTheme {
        GlassBackground {
            Row(
                modifier = Modifier.padding(Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                val description = stringResource(R.string.alarms_sample_toggle)
                GlassToggle(checked = true, onCheckedChange = {}, contentDescription = description)
                GlassToggle(checked = false, onCheckedChange = {}, contentDescription = description)
            }
        }
    }
}
