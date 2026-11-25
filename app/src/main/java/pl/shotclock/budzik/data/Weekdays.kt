package pl.shotclock.budzik.data

import java.time.DayOfWeek

/** Gotowe zestawy dni: do szybkich wyborów w edycji i do krótkiego opisu alarmu na liście. */
object Weekdays {
    val workdays = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
    )
    val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    val everyDay = DayOfWeek.entries.toSet()
}
