package pl.shotclock.budzik.alarm

import java.time.Duration
import java.time.LocalDateTime
import pl.shotclock.budzik.data.Alarm

// Czyste funkcje bez Androida: dostają aktualny czas jako parametr, więc w testach możemy podać dowolną datę.

private const val MINUTES_IN_HOUR = 60L
private const val MINUTES_IN_DAY = 24 * MINUTES_IN_HOUR
private const val MILLIS_IN_MINUTE = 60_000L

/** Najbliższy włączony alarm razem z chwilą, w której zadzwoni. */
data class NextAlarm(val alarm: Alarm, val ringTime: LocalDateTime)

/** Ile zostało do dzwonienia, rozbite na dni, godziny i minuty. */
data class TimeUntil(val days: Long, val hours: Long, val minutes: Long)

/**
 * Kiedy [alarm] zadzwoni następnym razem, licząc od [now].
 * Jeśli godzina alarmu już dziś minęła (albo trwa właśnie ta minuta), szukamy kolejnego pasującego dnia.
 */
fun nextRingTime(alarm: Alarm, now: LocalDateTime): LocalDateTime {
    val todayAtAlarmTime = now.toLocalDate().atTime(alarm.hour, alarm.minute)
    // Dziś i kolejne 7 dni: w takim oknie zawsze trafi się każdy dzień tygodnia.
    return (0L..7L)
        .map { daysAhead -> todayAtAlarmTime.plusDays(daysAhead) }
        .first { candidate ->
            val dayMatches = alarm.days.isEmpty() || candidate.dayOfWeek in alarm.days
            candidate.isAfter(now) && dayMatches
        }
}

/** Najbliższy z włączonych alarmów albo null, gdy żaden nie jest włączony. */
fun findNextAlarm(alarms: List<Alarm>, now: LocalDateTime): NextAlarm? =
    alarms
        .filter { it.enabled }
        .map { NextAlarm(alarm = it, ringTime = nextRingTime(it, now)) }
        .minByOrNull { it.ringTime }

/** Czas do dzwonienia. Minuty zaokrąglamy w górę, tak jak zegar w telefonie (6 h 59 min 30 s to „7 h”). */
fun timeUntil(now: LocalDateTime, ringTime: LocalDateTime): TimeUntil {
    val millis = Duration.between(now, ringTime).toMillis()
    val totalMinutes = (millis + MILLIS_IN_MINUTE - 1) / MILLIS_IN_MINUTE
    return TimeUntil(
        days = totalMinutes / MINUTES_IN_DAY,
        hours = totalMinutes % MINUTES_IN_DAY / MINUTES_IN_HOUR,
        minutes = totalMinutes % MINUTES_IN_HOUR,
    )
}
