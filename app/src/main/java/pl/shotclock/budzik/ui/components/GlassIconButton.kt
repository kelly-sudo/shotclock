package pl.shotclock.budzik.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.rememberPressScale
import pl.shotclock.budzik.ui.theme.scaledBy

@Composable
fun GlassIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GlassButtonStyle = GlassButtonStyle.Secondary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)

    Box(
        modifier = modifier
            .scaledBy(scale)
            .size(Sizes.iconButton)
            .buttonSurface(style, AppShapes.pill)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = buttonContentColor(style),
        )
    }
}

@Preview(widthDp = 120, heightDp = 100)
@Composable
private fun GlassIconButtonPreview() {
    ShotClockTheme {
        GlassBackground {
            GlassIconButton(
                icon = R.drawable.ic_arrow_back,
                contentDescription = stringResource(R.string.action_back),
                onClick = {},
                modifier = Modifier.padding(Spacing.medium),
            )
        }
    }
}

@Preview(widthDp = 120, heightDp = 100)
@Composable
private fun GlassIconButtonPrimaryPreview() {
    ShotClockTheme {
        GlassBackground {
            GlassIconButton(
                icon = R.drawable.ic_add,
                contentDescription = stringResource(R.string.action_add_alarm),
                onClick = {},
                modifier = Modifier.padding(Spacing.medium),
                style = GlassButtonStyle.Primary,
            )
        }
    }
}
