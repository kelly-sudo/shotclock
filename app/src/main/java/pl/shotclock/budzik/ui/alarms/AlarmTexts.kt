package pl.shotclock.budzik.ui.alarms

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import java.time.DayOfWeek
import java.util.Locale
import pl.shotclock.budzik.R
import pl.shotclock.budzik.alarm.TimeUntil
import pl.shotclock.budzik.data.Alarm
import pl.shotclock.budzik.data.Weekdays
import pl.shotclock.budzik.ui.objects.ObjectVisual

// Teksty opisujące alarm, wspólne dla listy alarmów i innych ekranów.

/** Dni alarmu w krótkiej formie, np. „Pn–Pt”, „So, Nd” albo „Pn, Śr, Pt”. */
@Composable
fun daysText(days: Set<DayOfWeek>): String {
    val shortNames = stringArrayResource(R.array.weekdays_short)
    return when {
        days.isEmpty() -> stringResource(R.string.alarms_days_once)
        days == Weekdays.everyDay -> stringResource(R.string.alarms_days_everyday)
        days == Weekdays.workdays -> stringResource(R.string.alarms_days_workdays)
        days == Weekdays.weekend -> stringResource(R.string.alarms_days_weekend)
        // Tablica nazw zaczyna się od poniedziałku (indeks 0), a DayOfWeek numeruje dni od 1.
        else -> days.sorted().joinToString(separator = ", ") { shortNames[it.value - 1] }
    }
}

/** Podpis pod godziną: dni i etykieta, np. „Pn–Pt · Zajęcia”. */
@Composable
fun alarmSubtitle(alarm: Alarm): String {
    val days = daysText(alarm.days)
    return if (alarm.label.isBlank()) days else stringResource(R.string.alarms_subtitle, days, alarm.label)
}

/** Wymagany przedmiot, np. „Zdjęcie: szczoteczka do zębów” albo „Zdjęcie: losowy przedmiot”. */
@Composable
fun alarmTaskText(alarm: Alarm): String {
    val objectName = stringResource(ObjectVisual.of(alarm.requiredObject).nameRes)
    // Nazwa przedmiotu w środku zdania zaczyna się małą literą.
    return stringResource(R.string.alarms_task, objectName.replaceFirstChar { it.lowercase() })
}

/** „Następny za 7 h 20 min” albo informacja, że żaden alarm nie jest włączony. */
@Composable
fun nextAlarmText(timeUntil: TimeUntil?): String = when {
    timeUntil == null -> stringResource(R.string.alarms_none_enabled)
    timeUntil.days > 0 -> stringResource(R.string.alarms_next_days, timeUntil.days, timeUntil.hours)
    timeUntil.hours > 0 -> stringResource(R.string.alarms_next_hours, timeUntil.hours, timeUntil.minutes)
    else -> stringResource(R.string.alarms_next_minutes, timeUntil.minutes)
}

/** Godzina w formacie „07:30”. */
fun formatTime(hour: Int, minute: Int): String = String.format(Locale.ROOT, "%02d:%02d", hour, minute)
