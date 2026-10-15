package moe.ktandth.songmeld.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

private val MaterialTypography = Typography()

/**
 * Material 3 typography with Google Sans Flex applied to every text role.
 *
 * Size, line height, letter spacing and weight continue to come from the
 * Material 3 defaults; only the font family is replaced.
 */
val SongMeldTypography =
  Typography(
    displayLarge = MaterialTypography.displayLarge.copy(fontFamily = GoogleSansFlexRounded),
    displayMedium = MaterialTypography.displayMedium.copy(fontFamily = GoogleSansFlexRounded),
    displaySmall = MaterialTypography.displaySmall.copy(fontFamily = GoogleSansFlexRounded),
    headlineLarge = MaterialTypography.headlineLarge.copy(fontFamily = GoogleSansFlexRounded),
    headlineMedium = MaterialTypography.headlineMedium.copy(fontFamily = GoogleSansFlexRounded),
    headlineSmall = MaterialTypography.headlineSmall.copy(fontFamily = GoogleSansFlexRounded),
    titleLarge = MaterialTypography.titleLarge.copy(fontFamily = GoogleSansFlexRounded),
    titleMedium = MaterialTypography.titleMedium.copy(fontFamily = GoogleSansFlexRounded),
    titleSmall = MaterialTypography.titleSmall.copy(fontFamily = GoogleSansFlexRounded),
    bodyLarge = MaterialTypography.bodyLarge.copy(fontFamily = GoogleSansFlexRounded),
    bodyMedium = MaterialTypography.bodyMedium.copy(fontFamily = GoogleSansFlexRounded),
    bodySmall = MaterialTypography.bodySmall.copy(fontFamily = GoogleSansFlexRounded),
    labelLarge = MaterialTypography.labelLarge.copy(fontFamily = GoogleSansFlexRounded),
    labelMedium = MaterialTypography.labelMedium.copy(fontFamily = GoogleSansFlexRounded),
    labelSmall = MaterialTypography.labelSmall.copy(fontFamily = GoogleSansFlexRounded),
  )

/**
 * Page-title variant of Google Sans Flex using stylistic set 02.
 *
 * This mirrors the OpenType feature used by Google web UIs:
 * `font-feature-settings: "ss02" on;`
 * Only screen-level titles opt into this style; the rest of SongMeld keeps
 * the regular rounded Google Sans Flex typography.
 */
val SongMeldPageTitleStyle: TextStyle =
  SongMeldTypography.titleLarge.copy(
    fontFeatureSettings = "\"ss02\" on",
  )

/** Consistent monospaced style for timecodes, technical values and LRC editing. */
val SongMeldCodeStyle: TextStyle =
  SongMeldTypography.bodySmall.copy(fontFamily = GoogleSansCode)
