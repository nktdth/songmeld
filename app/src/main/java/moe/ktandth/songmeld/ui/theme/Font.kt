@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package moe.ktandth.songmeld.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import moe.ktandth.songmeld.R

private val googleSansFlexWeights =
  listOf(
    FontWeight.Thin,
    FontWeight.ExtraLight,
    FontWeight.Light,
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
    FontWeight.ExtraBold,
    FontWeight.Black,
  )

private val googleSansCodeWeights =
  listOf(
    FontWeight.Light,
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
    FontWeight.ExtraBold,
  )

private fun googleSansFlexFontFamily(
  roundness: Float = 0f,
): FontFamily {
  require(roundness in 0f..100f) { "ROND must be in 0..100" }

  return FontFamily(
    *googleSansFlexWeights
      .map { weight ->
        Font(
          resId = R.font.google_sans_flex,
          weight = weight,
          style = FontStyle.Normal,
          variationSettings =
            FontVariation.Settings(
              weight,
              FontStyle.Normal,
              FontVariation.Setting("ROND", roundness),
            ),
        )
      }
      .toTypedArray(),
  )
}

/**
 * Fully rounded Google Sans Flex variant, ready for expressive accents.
 */
val GoogleSansFlexRounded = googleSansFlexFontFamily(roundness = 100f)

/**
 * Monospace family for lyrics timestamps, metadata/debug values and editors.
 *
 * Google Sans Code is also a variable font; one file covers weights 300..800.
 */
val GoogleSansCode =
  FontFamily(
    *googleSansCodeWeights
      .map { weight ->
        Font(
          resId = R.font.google_sans_code,
          weight = weight,
          style = FontStyle.Normal,
          variationSettings =
            FontVariation.Settings(
              weight,
              FontStyle.Normal,
              FontVariation.Setting("MONO", 1f),
            ),
        )
      }
      .toTypedArray(),
  )
