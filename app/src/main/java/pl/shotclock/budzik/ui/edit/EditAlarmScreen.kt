package pl.shotclock.budzik.ui.edit

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.AppChip
import pl.shotclock.budzik.ui.components.AppIconButton
import pl.shotclock.budzik.ui.components.AppToggle
import pl.shotclock.budzik.ui.components.ObjectTile
import pl.shotclock.budzik.ui.components.PresetChip
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.MintTint
import pl.shotclock.budzik.ui.theme.OverlineText
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.TimeTextLarge

private const val WheelNeighbourAlpha = 0.6f

// Numery dni: 0 to poniedziałek, 6 to niedziela.
private val Workdays = setOf(0, 1, 2, 3, 4)
private val Weekend = setOf(5, 6)
private val EveryDay = setOf(0, 1, 2, 3, 4, 5, 6)

/** Edycja alarmu na przykładowych danych. Przewijany wybór godziny i zapis do bazy powstaną w Etapie 2. */
@Composable
fun EditAlarmScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedDays by rememberSaveable { mutableStateOf(Workdays) }
    var selectedObject by rememberSaveable { mutableStateOf(ObjectVisual.Toothbrush) }

    ScreenContainer(
        title = stringResource(R.string.edit_title),
        modifier = modifier,
        onBack = onBack,
        trailing = {
            AppIconButton(
                icon = R.drawable.ic_delete,
                contentDescription = stringResource(R.string.action_delete_alarm),
                onClick = onBack,
            )
        },
    ) {
        TimeWheelCard()
        SectionTitle(R.string.edit_repeat)
        DayPresets(selectedDays, onSelect = { selectedDays = it })
        WeekdayChips(selectedDays, onChange = { selectedDays = it })
        SectionTitle(R.string.edit_object)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            ObjectVisual.entries.forEach { visual ->
                ObjectTile(
                    visual = visual,
                    selected = visual == selectedObject,
                    onClick = { selectedObject = visual },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        AlarmOptionsCard()
        AppButton(
            text = stringResource(R.string.edit_save),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionTitle(@StringRes textRes: Int) {
    Text(text = stringResource(textRes), style = OverlineText, color = TextSecondary)
}

/** Na razie tylko wygląd przewijanego wyboru godziny: wybrana wartość i jej sąsiedzi. */
@Composable
private fun TimeWheelCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tint = MintTint,
        shape = AppShapes.heroCard,
        contentPadding = PaddingValues(vertical = Spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WheelColumn(value = 7)
            Text(text = ":", style = TimeTextLarge, color = Accent)
            WheelColumn(value = 30)
        }
    }
}

@Composable
private fun WheelColumn(value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        WheelNeighbour(value - 1)
        Text(text = twoDigits(value), style = TimeTextLarge)
        WheelNeighbour(value + 1)
    }
}

@Composable
private fun WheelNeighbour(value: Int) {
    Text(
        text = twoDigits(value),
        style = MaterialTheme.typography.titleLarge,
        color = MintTint.secondaryText.copy(alpha = WheelNeighbourAlpha),
    )
}

private fun twoDigits(value: Int) = String.format(Locale.ROOT, "%02d", value)

@Composable
private fun DayPresets(selectedDays: Set<Int>, onSelect: (Set<Int>) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
        PresetChip(
            label = stringResource(R.string.edit_preset_workdays),
            selected = selectedDays == Workdays,
            onClick = { onSelect(Workdays) },
        )
        PresetChip(
            label = stringResource(R.string.edit_preset_weekend),
            selected = selectedDays == Weekend,
            onClick = { onSelect(Weekend) },
        )
        PresetChip(
            label = stringResource(R.string.edit_preset_everyday),
            selected = selectedDays == EveryDay,
            onClick = { onSelect(EveryDay) },
        )
    }
}

@Composable
private fun WeekdayChips(selectedDays: Set<Int>, onChange: (Set<Int>) -> Unit) {
    val shortNames = stringArrayResource(R.array.weekdays_short)
    val fullNames = stringArrayResource(R.array.weekdays_full)

    // FlowRow przenosi chipy do drugiego wiersza na wąskim ekranie (7 chipów po 48 dp nie zawsze się mieści).
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        shortNames.forEachIndexed { day, name ->
            AppChip(
                label = name,
                selected = day in selectedDays,
                onSelectedChange = { selected ->
                    onChange(if (selected) selectedDays + day else selectedDays - day)
                },
                contentDescription = fullNames[day],
            )
        }
    }
}

@Composable
private fun AlarmOptionsCard() {
    var vibration by rememberSaveable { mutableStateOf(true) }
    var risingVolume by rememberSaveable { mutableStateOf(true) }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Spacing.medium,
            end = Spacing.small,
            top = Spacing.extraSmall,
            bottom = Spacing.extraSmall,
        ),
    ) {
        OptionRow(R.drawable.ic_music, R.string.edit_sound) {
            Text(
                text = stringResource(R.string.edit_sound_sample),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
        }
        OptionRow(R.drawable.ic_vibration, R.string.edit_vibration) {
            AppToggle(
                checked = vibration,
                onCheckedChange = { vibration = it },
                contentDescription = stringResource(R.string.edit_vibration),
            )
        }
        OptionRow(R.drawable.ic_volume, R.string.edit_rising_volume) {
            AppToggle(
                checked = risingVolume,
                onCheckedChange = { risingVolume = it },
                contentDescription = stringResource(R.string.edit_rising_volume),
            )
        }
    }
}

@Composable
private fun OptionRow(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.height(Sizes.settingRowHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        // Opis nie jest potrzebny, bo obok ikony jest tekst.
        Icon(painterResource(icon), contentDescription = null)
        Text(
            text = stringResource(label),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        trailing()
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun EditAlarmScreenPreview() {
    ShotClockTheme {
        AppBackground {
            EditAlarmScreen(onBack = {}, onSave = {})
        }
    }
}
