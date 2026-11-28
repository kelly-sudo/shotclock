package pl.shotclock.budzik.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

// Statystyki pobudek jako czyste funkcje: nie znają Androida ani bazy, więc łatwo je sprawdzić testami (WakeStatsTest).

/** Wszystko, co pokazuje ekran sukcesu po wyłączeniu alarmu. */
data class WakeSummary(
    val event: WakeEvent,
    // Czas wstania: od początku dzwonienia do wyłączenia.
    val wakeUpSeconds: Int,
    val streakDays: Int,
    // Dni bieżącego tygodnia (od poniedziałku do niedzieli) z udaną pobudką.
    val goodDaysThisWeek: Set<DayOfWeek>,
    val today: DayOfWeek,
)

/** Czas wstania w sekundach. Nigdy ujemny, nawet gdyby zegar telefonu cofnął się w trakcie dzwonienia. */
fun wakeUpSeconds(event: WakeEvent): Int =
    ((event.dismissedAt - event.ringStartedAt) / 1000).coerceAtLeast(0).toInt()

/** Dzień, w którym alarm wyłączono, w strefie czasowej telefonu. */
fun WakeEvent.dismissedDate(zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(dismissedAt).atZone(zone).toLocalDate()

/**
 * Dni z udaną pobudką. Dzień jest udany, gdy był w nim choć jeden alarm i każdy wyłączono zdjęciem.
 * Jedno wyłączenie awaryjne psuje cały dzień.
 */
fun goodDays(events: List<WakeEvent>, zone: ZoneId): Set<LocalDate> =
    events
        .groupBy { it.dismissedDate(zone) }
        .filterValues { dayEvents -> dayEvents.none { it.emergency } }
        .keys

/**
 * Seria: ile kolejnych dni z alarmem, licząc od najnowszego, kończyło się zdjęciem.
 * Dzień bez żadnego alarmu (np. weekend bez budzika) serii nie przerywa. Przerywa ją wyłączenie awaryjne.
 */
fun currentStreak(events: List<WakeEvent>, zone: ZoneId): Int {
    val daysWithAlarm = events.map { it.dismissedDate(zone) }.distinct().sortedDescending()
    val good = goodDays(events, zone)
    return daysWithAlarm.takeWhile { it in good }.size
}

/** Udane dni w tygodniu, w którym jest [today] (od poniedziałku do niedzieli). */
fun goodDaysInWeek(events: List<WakeEvent>, today: LocalDate, zone: ZoneId): Set<DayOfWeek> {
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sunday = monday.plusDays(6)
    return goodDays(events, zone)
        .filter { !it.isBefore(monday) && !it.isAfter(sunday) }
        .map { it.dayOfWeek }
        .toSet()
}

/** Podsumowanie świeżo zapisanej pobudki [event] na tle całej historii [allEvents] (razem z nią). */
fun summarizeWake(event: WakeEvent, allEvents: List<WakeEvent>, zone: ZoneId): WakeSummary {
    val today = event.dismissedDate(zone)
    return WakeSummary(
        event = event,
        wakeUpSeconds = wakeUpSeconds(event),
        streakDays = currentStreak(allEvents, zone),
        goodDaysThisWeek = goodDaysInWeek(allEvents, today, zone),
        today = today.dayOfWeek,
    )
}
