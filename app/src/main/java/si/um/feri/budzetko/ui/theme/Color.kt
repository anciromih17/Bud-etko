package si.um.feri.budzetko.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val BudzetkoBackground = Color(0xFFF3F2FF)
val BudzetkoBorder = Color.Transparent
val BudzetkoSurface = Color(0xFFFFFFFF)
val BudzetkoInk = Color(0xFF050505)
val BudzetkoPurple = Color(0xFF6B4EFF)
val BudzetkoLime = Color(0xFFD9FF6C)

@Composable
fun budzetkoBackground(): Color = MaterialTheme.colorScheme.background

@Composable
fun budzetkoSurface(): Color = MaterialTheme.colorScheme.surface

@Composable
fun budzetkoInk(): Color = MaterialTheme.colorScheme.onSurface

@Composable
fun budzetkoMutedInk(): Color {
    return if (MaterialTheme.colorScheme.background.luminanceHint() < 0.5f) {
        Color(0xFFB8B3C4)
    } else {
        Color(0xFF6D6774)
    }
}

@Composable
fun budzetkoSoftAccent(): Color {
    return if (MaterialTheme.colorScheme.background.luminanceHint() < 0.5f) {
        Color(0xFF2B2436)
    } else {
        Color(0xFFFAF8FF)
    }
}

@Composable
fun budzetkoBorder(): Color {
    return if (MaterialTheme.colorScheme.background.luminanceHint() < 0.5f) {
        Color(0xFF342D42)
    } else {
        Color.Transparent
    }
}

private fun Color.luminanceHint(): Float {
    return (red * 0.299f + green * 0.587f + blue * 0.114f)
}
