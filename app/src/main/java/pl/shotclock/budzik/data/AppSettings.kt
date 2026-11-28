package pl.shotclock.budzik.data

import pl.shotclock.budzik.alarm.DefaultConfidenceThreshold

/**
 * Ustawienia całej aplikacji. Trzymamy je w DataStore (mały plik w telefonie), a nie w Room,
 * bo to kilka pojedynczych wartości, a nie tabela z wieloma wierszami.
 */
data class AppSettings(
    // Dźwięk i wibracje, z którymi startuje każdy nowy alarm. null oznacza domyślny dźwięk alarmu w telefonie.
    val defaultSoundUri: String? = null,
    val defaultVibration: Boolean = true,
    // true: rozpoznaje udawany model (FakeObjectClassifier), nawet gdy prawdziwy model jest już w aplikacji.
    val testMode: Boolean = true,
    // Ukryte opcje dla zespołu (próg pewności, podgląd ekranów). Włącza je 7 dotknięć wersji aplikacji.
    val developerMode: Boolean = false,
    // Od 0 do 1. Ile pewności modelu wystarczy, żeby uznać przedmiot za rozpoznany.
    val confidenceThreshold: Float = DefaultConfidenceThreshold,
    // false przy pierwszym uruchomieniu: wtedy aplikacja zaczyna od onboardingu (powitanie i zgody).
    val onboardingDone: Boolean = false,
)

/** Progi do wyboru w trybie deweloperskim. 55% to wartość ustalona z prowadzącym. */
val ConfidenceThresholdOptions = listOf(0.5f, 0.55f, 0.6f, 0.7f, 0.85f)

// Poniżej tej wartości alarm wyłączałby się od byle czego, a powyżej prawie nigdy.
private const val MinThreshold = 0.3f
private const val MaxThreshold = 0.95f

/** Pilnuje, żeby próg był rozsądny, nawet gdyby w pliku ustawień znalazła się dziwna wartość. */
fun safeThreshold(value: Float): Float =
    if (value.isNaN()) DefaultConfidenceThreshold else value.coerceIn(MinThreshold, MaxThreshold)
