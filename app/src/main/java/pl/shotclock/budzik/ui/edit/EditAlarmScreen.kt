package pl.shotclock.budzik.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.GlassBackground
import pl.shotclock.budzik.ui.components.GlassButton
import pl.shotclock.budzik.ui.components.GlassCard
import pl.shotclock.budzik.ui.components.GlassChip
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.components.TimeDisplay
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Spacing

/** Zaślepka edycji alarmu. Wybór godziny i zapis do bazy powstaną w Etapie 2. */
@Composable
fun EditAlarmScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContainer(
        title = stringResource(R.string.edit_title),
        modifier = modifier,
        onBack = onBack,
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            TimeDisplay(
                hour = 7,
                minute = 30,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Text(
            text = stringResource(R.string.edit_days),
            style = MaterialTheme.typography.titleMedium,
        )
        WeekdayChips()
        GlassButton(
            text = stringResource(R.string.edit_save),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WeekdayChips() {
    val shortNames = stringArrayResource(R.array.weekdays_short)
    val fullNames = stringArrayResource(R.array.weekdays_full)
    // Numery zaznaczonych dni: 0 to poniedziałek, 6 to niedziela.
    var selectedDays by rememberSaveable { mutableStateOf(setOf(0, 1, 2, 3, 4)) }

    // FlowRow przenosi chipy do drugiego wiersza na wąskim ekranie (7 chipów po 48 dp nie zawsze się mieści).
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        shortNames.forEachIndexed { day, name ->
            GlassChip(
                label = name,
                selected = day in selectedDays,
                onSelectedChange = { selected ->
                    selectedDays = if (selected) selectedDays + day else selectedDays - day
                },
                contentDescription = fullNames[day],
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun EditAlarmScreenPreview() {
    ShotClockTheme {
        GlassBackground {
            EditAlarmScreen(onBack = {}, onSave = {})
        }
    }
}
