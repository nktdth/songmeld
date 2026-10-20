package moe.ktandth.songmeld.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val MaterialTypography = Typography()

/**
 * Material 3 typography with Google Sans Flex applied to every text role.
 *
 * Body and control roles use a roomier reading scale. Sizes remain in sp, so
 * Android font scaling still applies. Display/headline roles retain Material defaults.
 */
val SongMeldTypography =
  Typography(
    displayLarge = MaterialTypography.displayLarge.copy(fontFamily = GoogleSansFlexRounded),
    displayMedium = MaterialTypography.displayMedium.copy(fontFamily = GoogleSansFlexRounded),
    displaySmall = MaterialTypography.displaySmall.copy(fontFamily = GoogleSansFlexRounded),
    headlineLarge = MaterialTypography.headlineLarge.copy(fontFamily = GoogleSansFlexRounded),
    headlineMedium = MaterialTypography.headlineMedium.copy(fontFamily = GoogleSansFlexRounded),
    headlineSmall = MaterialTypography.headlineSmall.copy(fontFamily = GoogleSansFlexRounded),
    titleLarge = MaterialTypography.titleLarge.copy(fontFamily = GoogleSansFlexRounded, fontSize = 24.sp, lineHeight = 30.sp),
    titleMedium = MaterialTypography.titleMedium.copy(fontFamily = GoogleSansFlexRounded, fontSize = 18.sp, lineHeight = 26.sp),
    titleSmall = MaterialTypography.titleSmall.copy(fontFamily = GoogleSansFlexRounded, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = MaterialTypography.bodyLarge.copy(fontFamily = GoogleSansFlexRounded, fontSize = 18.sp, lineHeight = 28.sp),
    bodyMedium = MaterialTypography.bodyMedium.copy(fontFamily = GoogleSansFlexRounded, fontSize = 16.sp, lineHeight = 24.sp),
    bodySmall = MaterialTypography.bodySmall.copy(fontFamily = GoogleSansFlexRounded, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = MaterialTypography.labelLarge.copy(fontFamily = GoogleSansFlexRounded, fontSize = 16.sp, lineHeight = 24.sp),
    labelMedium = MaterialTypography.labelMedium.copy(fontFamily = GoogleSansFlexRounded, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = MaterialTypography.labelSmall.copy(fontFamily = GoogleSansFlexRounded, fontSize = 12.sp, lineHeight = 16.sp),
  )

/** Consistent monospaced style for timecodes, technical values and LRC editing. */
val SongMeldCodeStyle: TextStyle =
  SongMeldTypography.bodySmall.copy(fontFamily = GoogleSansCode)
