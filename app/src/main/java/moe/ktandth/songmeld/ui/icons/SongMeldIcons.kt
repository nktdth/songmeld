package moe.ktandth.songmeld.ui.icons

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Size
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min

/** Material Symbols font families, rather than the legacy Filled Material Icons set. */
enum class AppIconStyle(val preferenceValue: String) {
    ROUNDED("rounded"), OUTLINED("outlined"), SHARP("sharp");

    companion object {
        fun fromPreference(value: String?): AppIconStyle =
            entries.firstOrNull { it.preferenceValue == value } ?: ROUNDED
    }
}

enum class AppIconShape(val preferenceValue: String) {
    CIRCLE("circle"), ROUNDED_SQUARE("rounded_square"), SQUARE("square"),
    SQUIRCLE("squircle"), PILL("pill"), HEXAGON("hexagon"), PENTAGON("pentagon"),
    TRIANGLE("triangle"), DIAMOND("diamond"), STAR("star"), FLOWER("flower"), CLOVER("clover");

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
        fill = if (fill >= 0.5f) 1f else 0f,
        weight = weight.coerceIn(300, 600),
        grade = grade.coerceIn(-25, 75),
        opticalSize = opticalSize.coerceIn(20, 40),
    )
}

/**
 * Stable interface icon identifiers backed by the complete Material Symbols fonts.
 * User-selected icon references use IconReference, rather than extending this enum.
 */
enum class SongMeldIconId(val codepoint: Int) {
    License(0xE90E), Code(0xE86F), Volunteer(0xEA70),
    ArrowBack(0xE5C4), ChevronRight(0xE5CC), Close(0xE5CD),
    FolderOpen(0xE2C8), Info(0xE88E), LibraryMusic(0xE030),
    Lock(0xE897), MoreVert(0xE5D4), MusicNote(0xE405),
    Palette(0xE40A), Refresh(0xE5D5), Search(0xE8B6),
    SearchOff(0xEA76), Settings(0xE8B8),
    LightMode(0xE518), DarkMode(0xE51C), SettingsBrightness(0xE8BD),
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
    SongMeldIconGlyph(
        codepoint = id.codepoint,
        pack = IconPack.SYMBOLS,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
        size = size,
        appearance = appearance,
    )
}

/** Background only: does not modify icon font axes or SongMeld logos. */
@Composable
fun songMeldIconShape(): Shape = iconBackgroundShape(LocalSongMeldIconShape.current)

fun iconBackgroundShape(shape: AppIconShape): Shape = when (shape) {
    AppIconShape.CIRCLE -> CircleShape
    AppIconShape.ROUNDED_SQUARE -> RoundedCornerShape(24.dp)
    AppIconShape.SQUARE -> RoundedCornerShape(8.dp)
    AppIconShape.SQUIRCLE -> RoundedCornerShape(percent = 38)
    AppIconShape.PILL -> RoundedCornerShape(percent = 50)
    else -> GenericShape { size, _ -> addPath(expressiveShapePath(size, shape)) }
}

/** Small reusable geometric shapes without a dependency on experimental MaterialShapes APIs. */
private fun expressiveShapePath(size: Size, shape: AppIconShape): Path {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = min(size.width, size.height) * 0.47f
    val segments = when (shape) {
        AppIconShape.HEXAGON -> 6
        AppIconShape.PENTAGON -> 5
        AppIconShape.TRIANGLE -> 3
        AppIconShape.DIAMOND -> 4
        AppIconShape.STAR -> 10
        AppIconShape.FLOWER -> 16
        AppIconShape.CLOVER -> 8
        else -> 8
    }
    val points = (0 until segments).map { index ->
        val angle = (2.0 * PI * index / segments) - PI / 2.0 +
            if (shape == AppIconShape.DIAMOND) PI / 4.0 else 0.0
        val r = when (shape) {
            AppIconShape.STAR -> if (index % 2 == 0) radius else radius * 0.52f
            AppIconShape.FLOWER -> if (index % 2 == 0) radius else radius * 0.8f
            AppIconShape.CLOVER -> if (index % 2 == 0) radius else radius * 0.59f
            else -> radius
        }
        Pair(cx + r * cos(angle).toFloat(), cy + r * sin(angle).toFloat())
    }
    return Path().apply {
        // Smooth most shapes with quadratic corners; star keeps its pronounced points.
        if (shape == AppIconShape.STAR) {
            moveTo(points.first().first, points.first().second)
            points.drop(1).forEach { lineTo(it.first, it.second) }
        } else {
            val previous = points.last()
            val first = points.first()
            moveTo((previous.first + first.first) / 2f, (previous.second + first.second) / 2f)
            points.forEachIndexed { index, current ->
                val next = points[(index + 1) % points.size]
                quadraticBezierTo(current.first, current.second,
                    (current.first + next.first) / 2f, (current.second + next.second) / 2f)
            }
        }
        close()
    }
}
