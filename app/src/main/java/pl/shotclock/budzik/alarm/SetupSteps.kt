package pl.shotclock.budzik.alarm

import android.os.Build

/** Zgoda albo ustawienie telefonu, o które prosimy na ostatniej karcie onboardingu. Kolejność = ważność. */
enum class SetupStep {
    // Android 13+: bez zgody na powiadomienia nie pokażemy ekranu dzwonienia.
    NOTIFICATIONS,

    // Tylko Android 12: dokładne alarmy włącza się w ustawieniach. Od Androida 13 budzik ma je od razu.
    EXACT_ALARMS,

    // Android 14+: ekran dzwonienia otwiera się sam na zablokowanym telefonie.
    FULL_SCREEN,

    // Kamera do zdjęcia przedmiotu (każda wersja Androida pyta o nią w okienku).
    CAMERA,

    // Optymalizacja baterii może uśpić budzik, zwłaszcza na Samsungu (One UI).
    BATTERY,
}

/**
 * Które kroki mają sens na telefonie z daną wersją Androida ([sdkInt], czyli numer API).
 * Czysta funkcja bez Androida w środku, więc sprawdza ją zwykły test (SetupStepsTest).
 */
fun setupStepsFor(sdkInt: Int): List<SetupStep> = buildList {
    if (sdkInt >= Build.VERSION_CODES.TIRAMISU) add(SetupStep.NOTIFICATIONS)
    if (sdkInt == Build.VERSION_CODES.S || sdkInt == Build.VERSION_CODES.S_V2) add(SetupStep.EXACT_ALARMS)
    if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) add(SetupStep.FULL_SCREEN)
    add(SetupStep.CAMERA)
    add(SetupStep.BATTERY)
}

/** Samsung (One UI) wyjątkowo chętnie usypia aplikacje, więc pokazujemy mu dokładniejszą podpowiedź. */
fun isSamsung(manufacturer: String = Build.MANUFACTURER): Boolean = manufacturer.equals("samsung", ignoreCase = true)
