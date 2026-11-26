package pl.shotclock.budzik.alarm

/** Po tylu milisekundach narastająca głośność dochodzi do maksimum. */
const val RISING_VOLUME_MILLIS = 30_000L

/** Głośność na starcie: cicho, ale słyszalnie. */
const val START_VOLUME = 0.1f

/**
 * Głośność odtwarzacza (od 0 do 1) po [elapsedMillis] od początku dzwonienia.
 * Bez narastania od razu jest pełna. Z narastaniem rośnie równo od [START_VOLUME] do 1.
 * 1 oznacza głośność alarmu ustawioną w telefonie, nie głośniej.
 */
fun ringVolume(elapsedMillis: Long, rising: Boolean): Float {
    if (!rising) return 1f
    val progress = (elapsedMillis.toFloat() / RISING_VOLUME_MILLIS).coerceIn(0f, 1f)
    return START_VOLUME + (1f - START_VOLUME) * progress
}
