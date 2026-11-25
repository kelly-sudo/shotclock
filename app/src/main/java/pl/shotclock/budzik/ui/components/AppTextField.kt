package pl.shotclock.budzik.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.SurfaceBorder
import pl.shotclock.budzik.ui.theme.SurfaceColor
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.TextSecondary

/**
 * Jednowierszowe pole tekstowe w stylu kart. Gdy pole jest aktywne, krawędź ma kolor akcentu.
 * [label] czyta czytnik ekranu, a [placeholder] widać, dopóki pole jest puste.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(if (isFocused) Accent else SurfaceBorder, label = "field border")
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.textFieldHeight)
            .clip(AppShapes.tile)
            .background(SurfaceColor)
            .border(Sizes.cardBorder, borderColor, AppShapes.tile)
            .semantics { contentDescription = label },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        singleLine = true,
        cursorBrush = SolidColor(Accent),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
        // „Gotowe” na klawiaturze chowa klawiaturę.
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.smallMedium),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                }
                innerTextField()
            }
        },
    )
}

@Preview(widthDp = 360, heightDp = 180)
@Composable
private fun AppTextFieldPreview() {
    ShotClockTheme {
        AppBackground {
            Column(Modifier.padding(Spacing.medium)) {
                var text by remember { mutableStateOf("") }
                AppTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = stringResource(R.string.edit_label),
                    placeholder = stringResource(R.string.edit_label_placeholder),
                )
                AppTextField(
                    value = "Zajęcia",
                    onValueChange = {},
                    label = stringResource(R.string.edit_label),
                    placeholder = stringResource(R.string.edit_label_placeholder),
                    modifier = Modifier.padding(top = Spacing.small),
                )
            }
        }
    }
}
