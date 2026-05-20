package si.um.feri.budzetko.ui.theme

import androidx.compose.ui.graphics.Color

val BudzetkoCategoryColors = listOf(
    Color(0xFFFFD84D),
    Color(0xFFBFAEF7),
    Color(0xFFFF9F6E),
    Color(0xFF9DE7B7),
    Color(0xFFFFB7CC),
    Color(0xFF8DD7F4),
    Color(0xFFD8F25D),
    Color(0xFFFFB864)
)

fun budzetkoCategoryColor(
    categoryId: Long,
    colorIndex: Int,
    hasEmoji: Boolean
): Color {
    val stableIndex = if (hasEmoji) {
        (categoryId - 1).coerceAtLeast(0).toInt()
    } else {
        colorIndex
    }
    return BudzetkoCategoryColors[stableIndex.coerceAtLeast(0) % BudzetkoCategoryColors.size]
}
