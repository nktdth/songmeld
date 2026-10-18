package moe.ktandth.songmeld.ui.icons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.ktandth.songmeld.R

/** Material Symbols font families, rather than the legacy Filled Material Icons set. */
enum class AppIconStyle(val preferenceValue: String) {
    ROUNDED("rounded"), OUTLINED("outlined"), SHARP("sharp");

    companion object {
        fun fromPreference(value: String?): AppIconStyle =
            entries.firstOrNull { it.preferenceValue == value } ?: ROUNDED
    }
}

enum class AppIconShape(val preferenceValue: String) {
    CIRCLE("circle"), ROUNDED_SQUARE("rounded_square"), SQUARE("square");

    companion object {
        fun fromPreference(value: String?): AppIconShape =
            entries.firstOrNull { it.preferenceValue == value } ?: CIRCLE
    }
}

/**
 * Values correspond to Google Material Symbols' variable axes.
 * Kept separate from background shape: changing the symbol must not change album covers or branding.
 */
data class MaterialSymbolAppearance(
    val style: AppIconStyle = AppIconStyle.ROUNDED,
    val fill: Float = 0f,
    val weight: Int = 400,
    val grade: Int = 0,
    val opticalSize: Int = 24,
) {
    fun validated() = copy(
        fill = fill.coerceIn(0f, 1f),
        weight = weight.coerceIn(100, 700),
        grade = grade.coerceIn(-50, 200),
        opticalSize = opticalSize.coerceIn(20, 48),
    )
}

/**
 * These Unicode codepoints come from Google's Material Symbols mappings.
 * The bundled variable fonts intentionally contain ONLY these glyphs; update the font subsets
 * when adding a new SongMeldIconId. No Android R.drawable integer goes into preferences.
 */
enum class SongMeldIconId(val codepoint: Int) {
    ArrowBack(0xE5C4), ChevronRight(0xE5CC), Close(0xE5CD),
    FolderOpen(0xE2C8), Info(0xE88E), LibraryMusic(0xE030),
    Lock(0xE897), MoreVert(0xE5D4), MusicNote(0xE405),
    Palette(0xE40A), Refresh(0xE5D5), Search(0xE8B6),
    SearchOff(0xEA76), Settings(0xE8B8),
}

val LocalMaterialSymbolAppearance = compositionLocalOf { MaterialSymbolAppearance() }
val LocalSongMeldIconShape = compositionLocalOf { AppIconShape.CIRCLE }

/** Compose Material Symbols rendered from bundled variable TTF glyphs, NOT legacy ImageVectors. */
@Composable
fun SongMeldIcon(
    id: SongMeldIconId,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    appearance: MaterialSymbolAppearance = LocalMaterialSymbolAppearance.current,
) {
    val config = appearance.validated()
    val family = remember(config) {
        val fontId = when (config.style) {
            AppIconStyle.ROUNDED -> R.font.material_symbols_rounded
            AppIconStyle.OUTLINED -> R.font.material_symbols_outlined
            AppIconStyle.SHARP -> R.font.material_symbols_sharp
        }
        FontFamily(
            Font(
                resId = fontId,
                variationSettings = FontVariation.Settings(
                    FontVariation.Setting("FILL", config.fill),
                    FontVariation.Setting("wght", config.weight.toFloat()),
                    FontVariation.Setting("GRAD", config.grade.toFloat()),
                    FontVariation.Setting("opsz", config.opticalSize.toFloat()),
                ),
            ),
        )
    }
    // Icons retain their dp size even when the user's system font scale is enlarged.
    val density = LocalDensity.current
    val iconTextSize = (size.value / density.fontScale).sp
    val iconSemantics = Modifier.clearAndSetSemantics {
        if (contentDescription != null) this.contentDescription = contentDescription
    }
    Box(
        modifier = modifier.size(size).then(iconSemantics),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = String(Character.toChars(id.codepoint)),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            style = TextStyle(
                color = tint,
                fontFamily = family,
                fontSize = iconTextSize,
                lineHeight = iconTextSize,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp,
            ),
        )
    }
}

/** Background only: does not modify icon font axes or SongMeld logos. */
@Composable
fun songMeldIconShape(): Shape = iconBackgroundShape(LocalSongMeldIconShape.current)

fun iconBackgroundShape(shape: AppIconShape): Shape = when (shape) {
    AppIconShape.CIRCLE -> CircleShape
    AppIconShape.ROUNDED_SQUARE -> RoundedCornerShape(24.dp)
    AppIconShape.SQUARE -> RoundedCornerShape(8.dp)
}
