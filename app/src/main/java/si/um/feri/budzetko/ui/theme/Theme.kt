package si.um.feri.budzetko.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BudzetkoPurple,
    secondary = PurpleGrey80,
    tertiary = BudzetkoLime,
    background = Color(0xFF111014),
    surface = Color(0xFF1B1822),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF050505),
    onBackground = Color(0xFFF7F4EF),
    onSurface = Color(0xFFF7F4EF)
)

private val LightColorScheme = lightColorScheme(
    primary = BudzetkoPurple,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = BudzetkoBackground,
    surface = BudzetkoSurface,
    onPrimary = Color.White,
    onSecondary = BudzetkoInk,
    onTertiary = Color.White,
    onBackground = BudzetkoInk,
    onSurface = BudzetkoInk
)

@Composable
fun BudzetkoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
