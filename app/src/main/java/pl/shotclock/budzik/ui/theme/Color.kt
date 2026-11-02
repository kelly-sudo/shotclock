package pl.shotclock.budzik.ui.theme

import androidx.compose.ui.graphics.Color

// Tło i powierzchnie. BackgroundColor musi być taki sam jak window_background w res/values/colors.xml.
val BackgroundColor = Color(0xFF0B0C10)
val SurfaceColor = Color(0xFF16181F)
val SurfaceBorder = Color(0xFF24262E)
val ControlBorder = Color(0xFF2F323B)
val ControlOffColor = Color(0xFF343741)
val ChartBarColor = Color(0xFF3A3D47)
val BottomBarShadow = Color.Black

// Teksty
val TextPrimary = Color(0xFFF5F7FA)
val TextSecondary = Color(0xFFA6ABB6)

// Jedyny kolor akcentu: aktywne przełączniki, główne przyciski, sukces rozpoznania.
val Accent = Color(0xFF5EEAD4)
val OnAccent = Color(0xFF06231F)
val AccentGlow = Accent.copy(alpha = 0.3f)

// Kolory do podświetlenia na ciemnym tle: dni tygodnia i aktywna zakładka.
val HighlightColor = TextPrimary
val OnHighlight = BackgroundColor

/** Komplet kolorów karty: tło, krawędź i tekst drugorzędny, dobrany tak, żeby był czytelny na tym tle. */
data class CardTint(
    val surface: Color,
    val border: Color,
    val secondaryText: Color,
)

val NeutralTint = CardTint(SurfaceColor, SurfaceBorder, TextSecondary)
val MintTint = CardTint(Color(0xFF0F2F2B), Accent.copy(alpha = 0.28f), Color(0xFFBFD6D2))
val PeachTint = CardTint(Color(0xFF3A2416), Color(0xFFF08A4B).copy(alpha = 0.3f), Color(0xFFE3CDBB))
val LilacTint = CardTint(Color(0xFF25213F), Color(0xFFC9C2FF).copy(alpha = 0.28f), Color(0xFFC9C2FF))
