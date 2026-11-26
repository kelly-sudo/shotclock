package pl.shotclock.budzik.alarm

import pl.shotclock.budzik.data.Alarm

/** Easter egg: alarm ustawiony na 21:37 gra specjalny utwór zamiast wybranego dźwięku. */
const val EASTER_EGG_HOUR = 21
const val EASTER_EGG_MINUTE = 37

/**
 * Nazwa pliku utworu w res/raw (bez rozszerzenia), np. res/raw/easter_2137.mp3.
 * Pliku nie ma w repozytorium: zespół musi wrzucić nagranie, do którego ma prawa.
 * Bez pliku alarm o 21:37 gra zwykły dźwięk.
 */
const val EASTER_EGG_SOUND_NAME = "easter_2137"

fun isEasterEggTime(hour: Int, minute: Int): Boolean =
    hour == EASTER_EGG_HOUR && minute == EASTER_EGG_MINUTE

/**
 * Dźwięki do wypróbowania po kolei, gdy [alarm] dzwoni. Serwis gra pierwszy, który da się odtworzyć.
 * Kolejność: utwór z easter egga (tylko o 21:37 i tylko gdy plik istnieje), dźwięk wybrany w alarmie,
 * a na końcu domyślne dźwięki telefonu. Adresy są zwykłymi tekstami, więc funkcję łatwo sprawdzić w teście.
 */
fun ringSoundCandidates(
    alarm: Alarm,
    easterEggSound: String?,
    defaultSounds: List<String>,
): List<String> {
    val easterEgg = easterEggSound?.takeIf { isEasterEggTime(alarm.hour, alarm.minute) }
    return listOfNotNull(easterEgg, alarm.soundUri) + defaultSounds
}
