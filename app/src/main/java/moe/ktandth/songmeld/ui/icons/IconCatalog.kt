package moe.ktandth.songmeld.ui.icons

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.ktandth.songmeld.R

/** In-memory cache: indexes are parsed once per pack, off the main thread, from local assets. */
object SongMeldIconCatalog {
    private val cache = ConcurrentHashMap<IconPack, List<CatalogIconEntry>>()

    suspend fun load(context: Context, pack: IconPack): List<CatalogIconEntry> = withContext(Dispatchers.IO) {
        cache[pack] ?: synchronized(cache) {
            cache[pack] ?: context.applicationContext.assets
                .open("icon_catalog/${pack.storageName}.tsv")
                .bufferedReader(Charsets.UTF_8)
                .useLines { lines -> parseIconCatalog(lines, pack) }
                .also { entries ->
                    check(entries.isNotEmpty()) { "Empty icon catalog: ${pack.storageName}" }
                    cache[pack] = entries
                }
        }
    }
}

/** Shared glyph renderer for interface symbols AND user-selected icons. */
@Composable
internal fun SongMeldIconGlyph(
    codepoint: Int,
    pack: IconPack,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    appearance: MaterialSymbolAppearance = LocalMaterialSymbolAppearance.current,
) {
    val config = appearance.validated()
    val fontId = when (pack) {
        IconPack.SYMBOLS -> when (config.style) {
            AppIconStyle.ROUNDED -> R.font.material_symbols_rounded
            AppIconStyle.OUTLINED -> R.font.material_symbols_outlined
            AppIconStyle.SHARP -> R.font.material_symbols_sharp
        }
        IconPack.MDI -> R.font.material_design_icons
        IconPack.FLUTTER -> R.font.flutter_material_icons
    }
    val family = remember(pack, fontId, if (pack == IconPack.SYMBOLS) config else null) {
        FontFamily(
            if (pack == IconPack.SYMBOLS) {
                Font(
                    resId = fontId,
                    variationSettings = FontVariation.Settings(
                        FontVariation.Setting("FILL", config.fill),
                        FontVariation.Setting("wght", config.weight.toFloat()),
                        FontVariation.Setting("GRAD", config.grade.toFloat()),
                        FontVariation.Setting("opsz", config.opticalSize.toFloat()),
                    ),
                )
            } else {
                Font(resId = fontId)
            },
        )
    }
    val density = LocalDensity.current
    val fontSize = (size.value / density.fontScale).sp
    val semantics = Modifier.clearAndSetSemantics {
        if (contentDescription != null) this.contentDescription = contentDescription
    }
    Box(
        modifier = modifier.size(size).then(semantics),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = String(Character.toChars(codepoint)),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            style = TextStyle(
                color = tint,
                fontFamily = family,
                fontSize = fontSize,
                lineHeight = fontSize,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp,
            ),
        )
    }
}

@Composable
fun CatalogIcon(
    reference: IconReference,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    appearance: MaterialSymbolAppearance = LocalMaterialSymbolAppearance.current,
) = SongMeldIconGlyph(
    codepoint = reference.codepoint,
    pack = reference.pack,
    contentDescription = contentDescription,
    modifier = modifier,
    tint = tint,
    size = size,
    appearance = appearance,
)
