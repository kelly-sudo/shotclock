package pl.shotclock.budzik.ui.theme

import androidx.compose.ui.unit.dp

object Spacing {
    val none = 0.dp
    val extraSmall = 4.dp
    val small = 8.dp
    val smallMedium = 12.dp
    val medium = 16.dp
    val screenEdge = 20.dp
    val large = 24.dp
    val extraLarge = 32.dp
}

object Sizes {
    // Minimalny rozmiar elementu dotykowego (wymaganie dostępności).
    val minTouchTarget = 48.dp
    val iconButton = 48.dp
    val floatingButton = 64.dp

    val cardBorder = 1.dp
    val selectedBorder = 2.dp
    val bottomBarShadow = 16.dp

    val toggleWidth = 52.dp
    val toggleHeight = 32.dp
    val toggleThumb = 24.dp
    val togglePadding = 4.dp

    val presetChipHeight = 40.dp
    val objectTileHeight = 96.dp
    // Wiersz opcji (np. Dźwięk, Wibracje): wyższy niż minimalny element dotykowy, żeby był wygodny.
    val optionRowHeight = 56.dp
    // Nazwa dzwonka w wierszu „Dźwięk” ucina się po tej szerokości.
    val soundTitleMaxWidth = 140.dp
    val textFieldHeight = 56.dp
    val snackbarHeight = 56.dp
    val listRowHeight = 56.dp
    val statusDot = 8.dp
    val modelDot = 10.dp
    val chartHeight = 104.dp
    // Miejsce nad słupkami na wartość dzisiejszego dnia, żeby zmieściła się nawet nad najwyższym słupkiem.
    val chartValueSpace = 24.dp
    // Najniższy słupek: krótki czas wstania i dzień bez pobudki też mają widoczną kreskę.
    val chartBarMin = 6.dp

    // Ilustracje przedmiotów
    val illustrationSmall = 32.dp
    val illustrationTile = 48.dp
    val illustrationRow = 44.dp
    val illustrationThumbnail = 56.dp
    val illustrationStreak = 72.dp
    val illustrationHero = 92.dp
    val illustrationCard = 112.dp
    val illustrationHistoryBox = 40.dp
    val illustrationOnboarding = 152.dp

    // Onboarding: kropki stron i znaczek „gotowe” przy zgodzie
    val pageDot = 8.dp
    val setupDoneBadge = 32.dp

    // Ekran dzwonienia: pole z falami, kółko i przedmiot w środku
    val ringHalo = 208.dp
    val ringCircle = 138.dp
    val ringObject = 100.dp

    // Ekran kamery: celownik, linia skanowania, pasek pewności, spust migawki i znaczek sukcesu
    val reticle = 248.dp
    val reticleCorner = 40.dp
    val reticleStroke = 4.dp
    val scanLine = 2.dp
    val confidenceBar = 8.dp
    val thresholdMark = 2.dp
    val shutter = 76.dp
    val shutterInner = 60.dp
    val shutterRing = 4.dp
    val successBadge = 88.dp
    val successIcon = 44.dp

    // Ekran sukcesu: rysowany ptaszek w kółku, iskry wokół niego i kółka dni tygodnia
    val successCheck = 168.dp
    val successCheckStroke = 5.dp
    val successTickStroke = 9.dp
    val successSpark = 9.dp
    // Jak daleko od środka ptaszka dolatują iskry.
    val successSparkReach = 130.dp
    val weekDay = 36.dp
    // Obwódka wokół dzisiejszego dnia tygodnia.
    val weekDayTodayRing = 3.dp

    // Wyjście awaryjne: klawisz klawiatury numerycznej i pole z wynikiem
    // Klawisz jest niższy niż zwykły przycisk, żeby cała klawiatura zmieściła się na ekranie 360 × 640 dp.
    val keypadKey = 52.dp
    val answerField = 56.dp

    // Tyle miejsca na dole ekranu zostawiamy, żeby treść dało się przewinąć ponad dolny pasek.
    val bottomBarClearance = 104.dp
}
