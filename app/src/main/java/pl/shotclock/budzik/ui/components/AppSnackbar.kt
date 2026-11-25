package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.cardSurface

/**
 * Krótki komunikat na dole ekranu z opcjonalnym przyciskiem, np. „Usunięto alarm · Cofnij”.
 * Kiedy go pokazać i schować, decyduje SnackbarHost z Material 3; ten komponent odpowiada tylko za wygląd.
 */
@Composable
fun AppSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.snackbarHeight)
            .cardSurface(shape = AppShapes.tile)
            .padding(start = Spacing.medium, end = Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Spacing.smallMedium),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        if (actionLabel != null) {
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = Sizes.minTouchTarget, minHeight = Sizes.minTouchTarget)
                    .clip(AppShapes.pill)
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = Spacing.smallMedium),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge, color = Accent)
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 120)
@Composable
private fun AppSnackbarPreview() {
    ShotClockTheme {
        AppBackground {
            AppSnackbar(
                message = stringResource(R.string.alarms_deleted, "07:30"),
                actionLabel = stringResource(R.string.alarms_undo),
                modifier = Modifier.padding(Spacing.medium),
            )
        }
    }
}
