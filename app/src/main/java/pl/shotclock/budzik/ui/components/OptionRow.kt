package pl.shotclock.budzik.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary

/**
 * Wiersz opcji w karcie: ikona, nazwa (opcjonalnie z opisem pod spodem) i coś po prawej, np. przełącznik.
 * Używają go edycja alarmu i ustawienia. Gdy podamy [onClick], cały wiersz da się kliknąć.
 */
@Composable
fun OptionRow(
    label: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    // Wiersz rośnie, gdy przy dużej czcionce opis łamie się na dwie linie.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.optionRowHeight)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        // Opis nie jest potrzebny, bo obok ikony jest tekst.
        if (icon != null) Icon(painterResource(icon), contentDescription = null)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Spacing.small),
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        trailing()
    }
}

@Preview(widthDp = 360, heightDp = 200)
@Composable
private fun OptionRowPreview() {
    ShotClockTheme {
        AppBackground {
            AppCard {
                var checked by remember { mutableStateOf(true) }
                val vibration = stringResource(R.string.edit_vibration)
                OptionRow(label = vibration, icon = R.drawable.ic_vibration) {
                    AppToggle(checked = checked, onCheckedChange = { checked = it }, contentDescription = vibration)
                }
                OptionRow(
                    label = stringResource(R.string.settings_test_mode),
                    description = stringResource(R.string.settings_test_mode_description),
                )
            }
        }
    }
}
