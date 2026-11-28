package pl.shotclock.budzik.alarm

/** Po tylu nieudanych zdjęciach pojawia się wyjście awaryjne. */
const val EmergencyFailedAttempts = 10

/** Po takim czasie dzwonienia (10 minut) pojawia się wyjście awaryjne, nawet bez nieudanych zdjęć. */
const val EmergencyRingingMillis = 10 * 60 * 1000L

/**
 * Czy pokazać „Nie mogę znaleźć przedmiotu”. Czysta funkcja, sprawdzana w EmergencyExitTest.
 * Wyjście awaryjne jest po to, żeby nie dało się „utknąć” z dzwoniącym alarmem, np. gdy przedmiot zginął.
 */
fun isEmergencyExitAvailable(failedAttempts: Int, ringingForMillis: Long): Boolean =
    failedAttempts >= EmergencyFailedAttempts || ringingForMillis >= EmergencyRingingMillis
