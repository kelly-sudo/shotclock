package pl.shotclock.budzik.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.shotclock.budzik.R

/** Czas w sekundach jako tekst, np. „1 min 42 s”, „3 min” albo „47 s”. Używają go historia i ekran sukcesu. */
@Composable
fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val rest = seconds % 60
    return when {
        minutes == 0 -> stringResource(R.string.duration_seconds, rest)
        rest == 0 -> stringResource(R.string.duration_minutes, minutes)
        else -> stringResource(R.string.duration_minutes_seconds, minutes, rest)
    }
}
