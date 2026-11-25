package pl.shotclock.budzik.ui.edit

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import pl.shotclock.budzik.R
import pl.shotclock.budzik.data.Alarm
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.data.Weekdays
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.AppChip
import pl.shotclock.budzik.ui.components.AppIconButton
import pl.shotclock.budzik.ui.components.AppTextField
import pl.shotclock.budzik.ui.components.AppToggle
import pl.shotclock.budzik.ui.components.ObjectTile
import pl.shotclock.budzik.ui.components.PresetChip
import pl.shotclock.budzik.ui.components.ScreenContainer
import pl.shotclock.budzik.ui.components.TimeWheel
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.OverlineText
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.viewmodel.AppViewModelProvider
import pl.shotclock.budzik.viewmodel.EditAlarmUiState
import pl.shotclock.budzik.viewmodel.EditAlarmViewModel

/** Akcje, które ekran edycji przekazuje dalej. Zebrane w jednym miejscu, żeby nie podawać ich osobno. */
data class EditAlarmActions(
    val onBack: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onTimeChange: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    val onDaysChange: (Set<DayOfWeek>) -> Unit = {},
    val onLabelChange: (String) -> Unit = {},
    val onRequiredObjectChange: (RequiredObject) -> Unit = {},
    val onVibrationChange: (Boolean) -> Unit = {},
    val onRisingVolumeChange: (Boolean) -> Unit = {},
)

/**
 * Ekran edycji z ViewModelem. [onDelete] dostaje id alarmu: usuwa go lista alarmów,
 * żeby po powrocie mogła pokazać przycisk „Cofnij”.
 */
@Composable
fun EditAlarmRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDelete: (alarmId: Long) -> Unit,
    viewModel: EditAlarmViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EditAlarmScreen(
        uiState = uiState,
        actions = EditAlarmActions(
            onBack = onBack,
            onSave = { viewModel.save(onSaved = onSaved) },
            onDelete = { onDelete(uiState.alarm.id) },
            onTimeChange = viewModel::setTime,
            onDaysChange = viewModel::setDays,
            onLabelChange = viewModel::setLabel,
            onRequiredObjectChange = viewModel::setRequiredObject,
            onVibrationChange = viewModel::setVibration,
            onRisingVolumeChange = viewModel::setRisingVolume,
        ),
    )
}

/** Wygląd ekranu edycji. Dostaje gotowy stan, więc działa też w @Preview, bez bazy. */
@Composable
fun EditAlarmScreen(
    uiState: EditAlarmUiState,
    actions: EditAlarmActions,
    modifier: Modifier = Modifier,
) {
    ScreenContainer(
        title = stringResource(if (uiState.isNew) R.string.edit_title_new else R.string.edit_title),
        modifier = modifier,
        onBack = actions.onBack,
        trailing = {
            // Nowego alarmu nie ma jeszcze w bazie, więc nie ma czego usuwać.
            if (!uiState.isNew) {
                AppIconButton(
                    icon = R.drawable.ic_delete,
                    contentDescription = stringResource(R.string.action_delete_alarm),
                    onClick = actions.onDelete,
                )
            }
        },
        // „Zapisz alarm” jest zawsze widoczny na dole, niezależnie od przewinięcia.
        pinnedBottom = {
            if (!uiState.isLoading) {
                AppButton(
                    text = stringResource(R.string.edit_save),
                    onClick = actions.onSave,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) {
        // Zanim alarm wczyta się z bazy, pokazujemy tylko tytuł.
        if (!uiState.isLoading) {
            EditAlarmForm(uiState.alarm, actions)
        }
    }
}

/** Pola formularza. Funkcja dla ColumnScope, więc pola trafiają prosto do kolumny ekranu, z jej odstępami. */
@Composable
private fun ColumnScope.EditAlarmForm(alarm: Alarm, actions: EditAlarmActions) {
    TimeCard(alarm, actions.onTimeChange)
    SectionTitle(R.string.edit_repeat)
    DayPresets(alarm.days, onSelect = actions.onDaysChange)
    WeekdayChips(alarm.days, onChange = actions.onDaysChange)
    if (alarm.days.isEmpty()) {
        Text(
            text = stringResource(R.string.edit_days_once_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
    SectionTitle(R.string.edit_label)
    AppTextField(
        value = alarm.label,
        onValueChange = actions.onLabelChange,
        label = stringResource(R.string.edit_label),
        placeholder = stringResource(R.string.edit_label_placeholder),
    )
    SectionTitle(R.string.edit_object)
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
        ObjectVisual.entries.forEach { visual ->
            ObjectTile(
                visual = visual,
                selected = visual.requiredObject == alarm.requiredObject,
                onClick = { actions.onRequiredObjectChange(visual.requiredObject) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    AlarmOptionsCard(alarm, actions)
}

@Composable
private fun SectionTitle(@StringRes textRes: Int) {
    Text(text = stringResource(textRes), style = OverlineText, color = TextSecondary)
}

/** Karta z wyborem godziny w kolorze wybranego przedmiotu. */
@Composable
private fun TimeCard(alarm: Alarm, onTimeChange: (hour: Int, minute: Int) -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tint = ObjectVisual.of(alarm.requiredObject).tint,
        shape = AppShapes.heroCard,
        contentPadding = PaddingValues(vertical = Spacing.small),
    ) {
        TimeWheel(
            hour = alarm.hour,
            minute = alarm.minute,
            onTimeChange = onTimeChange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DayPresets(selectedDays: Set<DayOfWeek>, onSelect: (Set<DayOfWeek>) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
        PresetChip(
            label = stringResource(R.string.edit_preset_workdays),
            selected = selectedDays == Weekdays.workdays,
            onClick = { onSelect(Weekdays.workdays) },
        )
        PresetChip(
            label = stringResource(R.string.edit_preset_weekend),
            selected = selectedDays == Weekdays.weekend,
            onClick = { onSelect(Weekdays.weekend) },
        )
        PresetChip(
            label = stringResource(R.string.edit_preset_everyday),
            selected = selectedDays == Weekdays.everyDay,
            onClick = { onSelect(Weekdays.everyDay) },
        )
    }
}

@Composable
private fun WeekdayChips(selectedDays: Set<DayOfWeek>, onChange: (Set<DayOfWeek>) -> Unit) {
    // Obie tablice zaczynają się od poniedziałku, tak jak DayOfWeek.entries.
    val shortNames = stringArrayResource(R.array.weekdays_short)
    val fullNames = stringArrayResource(R.array.weekdays_full)

    // Siedem chipów dzieli szerokość po równo, więc zawsze mieszczą się w jednym wierszu.
    // Na wąskim ekranie (360 dp) są nieco węższe niż 48 dp, ale wysokość zostaje 48 dp.
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
        DayOfWeek.entries.forEachIndexed { index, day ->
            AppChip(
                label = shortNames[index],
                selected = day in selectedDays,
                onSelectedChange = { selected ->
                    onChange(if (selected) selectedDays + day else selectedDays - day)
                },
                contentDescription = fullNames[index],
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AlarmOptionsCard(alarm: Alarm, actions: EditAlarmActions) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Spacing.medium,
            end = Spacing.small,
            top = Spacing.extraSmall,
            bottom = Spacing.extraSmall,
        ),
    ) {
        // Wybór dźwięku dojdzie w Etapie 3, razem z odtwarzaniem. Na razie zawsze jest domyślny.
        OptionRow(R.drawable.ic_music, R.string.edit_sound) {
            Text(
                text = stringResource(R.string.edit_sound_default),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        OptionRow(R.drawable.ic_vibration, R.string.edit_vibration) {
            AppToggle(
                checked = alarm.vibration,
                onCheckedChange = actions.onVibrationChange,
                contentDescription = stringResource(R.string.edit_vibration),
            )
        }
        OptionRow(R.drawable.ic_volume, R.string.edit_rising_volume) {
            AppToggle(
                checked = alarm.risingVolume,
                onCheckedChange = actions.onRisingVolumeChange,
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
    // Wiersz rośnie, gdy przy dużej czcionce opis łamie się na dwie linie.
    Row(
        modifier = Modifier.heightIn(min = Sizes.settingRowHeight),
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

@Preview(widthDp = 390, heightDp = 1100)
@Composable
private fun EditAlarmScreenPreview() {
    ShotClockTheme {
        AppBackground {
            EditAlarmScreen(
                uiState = EditAlarmUiState(
                    isNew = false,
                    alarm = Alarm(id = 1, hour = 7, minute = 30, days = Weekdays.workdays, label = "Zajęcia"),
                ),
                actions = EditAlarmActions(),
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 1100)
@Composable
private fun EditAlarmScreenNewOneTimePreview() {
    ShotClockTheme {
        AppBackground {
            EditAlarmScreen(
                uiState = EditAlarmUiState(
                    alarm = Alarm(hour = 6, minute = 15, requiredObject = RequiredObject.SECOND),
                ),
                actions = EditAlarmActions(),
            )
        }
    }
}
