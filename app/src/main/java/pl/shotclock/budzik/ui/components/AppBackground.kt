package pl.shotclock.budzik.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.ui.theme.BackgroundColor
import pl.shotclock.budzik.ui.theme.ShotClockTheme

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor),
        content = content,
    )
}

@Preview(widthDp = 360, heightDp = 200)
@Composable
private fun AppBackgroundPreview() {
    ShotClockTheme {
        AppBackground {}
    }
}
