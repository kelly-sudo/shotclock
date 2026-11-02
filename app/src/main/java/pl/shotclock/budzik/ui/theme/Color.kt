package pl.shotclock.budzik.ui.theme

import androidx.compose.ui.graphics.Color

// Tło aplikacji. GradientNavy musi być taki sam jak window_background w res/values/colors.xml.
val GradientNavy = Color(0xFF0B1026)
val GradientViolet = Color(0xFF2A1B5E)
val GradientBlue = Color(0xFF0E2A5C)
val BackgroundGradient = listOf(GradientNavy, GradientViolet, GradientBlue)

// Szkło
val GlassFill = Color.White.copy(alpha = 0.15f)
val GlassBorder = Color.White.copy(alpha = 0.25f)
val GlassSheen = Color.White.copy(alpha = 0.12f)
val GlassShadow = Color.Black.copy(alpha = 0.25f)

// Teksty
val TextPrimary = Color(0xFFF5F7FA)
val TextSecondary = Color(0xFFD6DBE7)

// Jedyny kolor akcentu: aktywne przełączniki, główne przyciski, sukces rozpoznania.
val Accent = Color(0xFF5EEAD4)
val OnAccent = Color(0xFF06231F)
