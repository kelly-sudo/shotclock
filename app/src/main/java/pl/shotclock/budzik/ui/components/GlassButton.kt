package pl.shotclock.budzik.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.OnAccent
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.accentSurface
import pl.shotclock.budzik.ui.theme.glassSurface
import pl.shotclock.budzik.ui.theme.rememberPressScale
import pl.shotclock.budzik.ui.theme.scaledBy

/** Primary: wypełniony akcentem, do głównej akcji na ekranie. Secondary: szklany. */
enum class GlassButtonStyle { Primary, Secondary }

internal fun Modifier.buttonSurface(style: GlassButtonStyle, shape: Shape): Modifier =
    when (style) {
        GlassButtonStyle.Primary -> accentSurface(shape)
        GlassButtonStyle.Secondary -> glassSurface(shape)
    }

internal fun buttonContentColor(style: GlassButtonStyle): Color =
    when (style) {
        GlassButtonStyle.Primary -> OnAccent
        GlassButtonStyle.Secondary -> TextPrimary
    }

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GlassButtonStyle = GlassButtonStyle.Primary,
    @DrawableRes icon: Int? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    val contentColor = buttonContentColor(style)

    Row(
        modifier = modifier
            .scaledBy(scale)
            .buttonSurface(style, AppShapes.pill)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            // Opis nie jest potrzebny, bo przycisk ma obok ikony tekst.
            Icon(painterResource(icon), contentDescription = null, tint = contentColor)
            Spacer(Modifier.width(Spacing.small))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = contentColor)
    }
}

@Preview(widthDp = 360, heightDp = 120)
@Composable
private fun GlassButtonPrimaryPreview() {
    ShotClockTheme {
        GlassBackground {
            GlassButton(
                text = stringResource(R.string.ring_open_camera),
                onClick = {},
                modifier = Modifier.padding(Spacing.medium),
                icon = R.drawable.ic_camera,
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 120)
@Composable
private fun GlassButtonSecondaryPreview() {
    ShotClockTheme {
        GlassBackground {
            GlassButton(
                text = stringResource(R.string.settings_preview_camera),
                onClick = {},
                modifier = Modifier.padding(Spacing.medium),
                style = GlassButtonStyle.Secondary,
            )
        }
    }
}
