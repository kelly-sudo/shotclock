package pl.shotclock.budzik.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// FontFamily.SansSerif, bo na API 26–27 Compose tylko dla tej rodziny znajduje cieńsze warianty czcionki.
private val AppFont = FontFamily.SansSerif

val Typography = Typography(
    headlineMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

// "tnum" nadaje wszystkim cyfrom tę samą szerokość, więc godzina nie „skacze” przy zmianie minut.
private val TimeTextBase = TextStyle(
    fontFamily = AppFont,
    fontWeight = FontWeight.Thin,
    fontFeatureSettings = "tnum",
)
val TimeTextLarge = TimeTextBase.copy(fontSize = 64.sp, lineHeight = 72.sp)
val TimeTextHuge = TimeTextBase.copy(fontSize = 112.sp, lineHeight = 120.sp)
