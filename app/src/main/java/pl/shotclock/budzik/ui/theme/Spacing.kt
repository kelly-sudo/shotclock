package pl.shotclock.budzik.ui.theme

import androidx.compose.ui.unit.dp

object Spacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
}

object Sizes {
    // Minimalny rozmiar elementu dotykowego (wymaganie dostępności).
    val minTouchTarget = 48.dp
    val iconButton = 56.dp

    val glassBorder = 1.dp
    val shadowRadius = 24.dp
    val shadowOffset = 8.dp

    val toggleWidth = 52.dp
    val toggleHeight = 32.dp
    val toggleThumb = 24.dp
    val togglePadding = 4.dp

    // Tyle miejsca na dole ekranu zostawiamy, żeby treść dało się przewinąć ponad dolny pasek.
    val bottomBarClearance = 104.dp
}
