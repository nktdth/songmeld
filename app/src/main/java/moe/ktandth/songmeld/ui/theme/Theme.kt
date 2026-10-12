package moe.ktandth.songmeld.ui.theme

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

// Static palette for Android 8–11, shared with the orange/brown launcher identity.
// The stronger orange primary ensures readable labels on a pale surface;
// #FFA069 is preserved verbatim for the primary container and launcher badge.
private val SongMeldLightColors = lightColorScheme(
    primary = Color(0xFF984313),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFA069),
    onPrimaryContainer = Color(0xFF57331E),
    secondary = Color(0xFF80533D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDDCB),
    onSecondaryContainer = Color(0xFF3D2418),
    tertiary = Color(0xFF775B43),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF2DEC9),
    onTertiaryContainer = Color(0xFF332516),
    background = Color(0xFFFFF5EF),
    onBackground = Color(0xFF2C1B14),
    surface = Color(0xFFFFF5EF),
    onSurface = Color(0xFF2C1B14),
    surfaceVariant = Color(0xFFF4E1D7),
    onSurfaceVariant = Color(0xFF68544A),
    surfaceContainerLow = Color(0xFFFFEDE4),
    surfaceContainer = Color(0xFFFFE6D9),
    surfaceContainerHigh = Color(0xFFFFDFD1),
    outline = Color(0xFF9C8579),
    outlineVariant = Color(0xFFE7CEC1),
)

private val SongMeldDarkColors = darkColorScheme(
    primary = Color(0xFFFFB68C),
    onPrimary = Color(0xFF552700),
    primaryContainer = Color(0xFF743D1E),
    onPrimaryContainer = Color(0xFFFFDAC3),
    secondary = Color(0xFFE5BBA6),
    onSecondary = Color(0xFF452A1E),
    secondaryContainer = Color(0xFF604536),
    onSecondaryContainer = Color(0xFFFFDCCC),
    tertiary = Color(0xFFDDC1A4),
    onTertiary = Color(0xFF402E1E),
    tertiaryContainer = Color(0xFF59432C),
    onTertiaryContainer = Color(0xFFFADFC0),
    background = Color(0xFF24170F),
    onBackground = Color(0xFFF6EBE4),
    surface = Color(0xFF24170F),
    onSurface = Color(0xFFF6EBE4),
    surfaceVariant = Color(0xFF504037),
    onSurfaceVariant = Color(0xFFDBC3B7),
    surfaceContainerLow = Color(0xFF2B1E17),
    surfaceContainer = Color(0xFF34251C),
    surfaceContainerHigh = Color(0xFF3E2D23),
    outline = Color(0xFFA99183),
    outlineVariant = Color(0xFF5B473C),
)

@Composable
fun SongMeldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        // On Android 12+, retain wallpaper-derived Material You colors.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> SongMeldDarkColors
        else -> SongMeldLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
