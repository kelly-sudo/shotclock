package pl.shotclock.budzik.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.glassSurface

data class BottomBarItem(
    val route: String,
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
)

@Composable
fun GlassBottomBar(
    items: List<BottomBarItem>,
    selectedRoute: String?,
    onItemClick: (BottomBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.medium)
            .padding(bottom = Spacing.medium)
            .glassSurface(AppShapes.card)
            .padding(Spacing.small),
    ) {
        items.forEach { item ->
            val selected = item.route == selectedRoute
            val color = if (selected) Accent else TextSecondary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(AppShapes.card)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onItemClick(item) })
                    .padding(vertical = Spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            ) {
                // Opis nie jest potrzebny, bo zakładka ma pod ikoną tekst.
                Icon(painterResource(item.icon), contentDescription = null, tint = color)
                Text(
                    text = stringResource(item.label),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 120)
@Composable
private fun GlassBottomBarPreview() {
    ShotClockTheme {
        GlassBackground {
            Box(Modifier.align(Alignment.BottomCenter)) {
                GlassBottomBar(
                    items = listOf(
                        BottomBarItem("a", R.string.tab_alarms, R.drawable.ic_alarm),
                        BottomBarItem("b", R.string.tab_history, R.drawable.ic_history),
                        BottomBarItem("c", R.string.tab_objects, R.drawable.ic_category),
                        BottomBarItem("d", R.string.tab_settings, R.drawable.ic_settings),
                    ),
                    selectedRoute = "a",
                    onItemClick = {},
                )
            }
        }
    }
}
