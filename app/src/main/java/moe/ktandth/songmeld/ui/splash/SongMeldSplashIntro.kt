package moe.ktandth.songmeld.ui.splash

import android.graphics.PathMeasure
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.R
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sin

private const val LOGO_CENTER = 12f
private const val VIEWPORT_SIZE = 24f
private const val OUTLINE_SAMPLES = 160

/**
 * Original SongMeld outer badge outline, matching branding/songmeld-logo.svg.
 *
 * The static system splash and this animated Compose version share the same
 * silhouette. Only the points sampled along its edge are displaced; the
 * wrench-note is drawn independently with the launcher foreground color.
 *
 * Keep this path in sync if the approved brand logo changes.
 */
private const val BADGE_PATH = """M9.83323 0.900276C11.0232 -0.300092 12.9761 -0.300092 14.166 0.900276C14.8607 1.601 15.8571 1.92162 16.835 1.75908C18.5101 1.48063 20.0901 2.6174 20.3403 4.28119C20.4864 5.25244 21.1022 6.09184 21.9898 6.52956C23.5103 7.27939 24.1138 9.11873 23.3287 10.6104C22.8704 11.4812 22.8704 12.5188 23.3287 13.3896C24.1138 14.8813 23.5103 16.7206 21.9898 17.4704C21.1022 17.9082 20.4864 18.7476 20.3403 19.7188C20.0901 21.3826 18.5101 22.5194 16.835 22.2409C15.8571 22.0784 14.8607 22.399 14.166 23.0997C12.9761 24.3001 11.0232 24.3001 9.83323 23.0997C9.13859 22.399 8.14218 22.0784 7.16429 22.2409C5.48914 22.5194 3.90922 21.3826 3.65898 19.7188C3.5129 18.7476 2.89708 17.9082 2.00948 17.4704C0.48899 16.7206 -0.114487 14.8813 0.670568 13.3896C1.12885 12.5188 1.12885 11.4812 0.670568 10.6104C-0.114488 9.11873 0.48899 7.27939 2.00948 6.52956C2.89708 6.09184 3.5129 5.25244 3.65898 4.28119C3.90922 2.6174 5.48914 1.48063 7.16429 1.75908C8.14218 1.92162 9.13859 1.601 9.83323 0.900276Z"""

/** One measured perimeter sample in the brand logo's 24x24 coordinate space. */
private data class BadgePoint(val position: Offset, val angle: Float)

/**
 * Cache equally spaced samples from the original vector path once.
 *
 * PathMeasure is only used during composition, never on every animation
 * frame. Drawing uses these cached points and cheap trigonometry.
 */
private fun sampleBadge(): List<BadgePoint> {
    val path = PathParser().parsePathString(BADGE_PATH).toPath()
    val measure = PathMeasure(path.asAndroidPath(), true)
    val coords = FloatArray(2)
    return List(OUTLINE_SAMPLES) { index ->
        measure.getPosTan(measure.length * index / OUTLINE_SAMPLES, coords, null)
        val x = coords[0]
        val y = coords[1]
        BadgePoint(Offset(x, y), atan2(y - LOGO_CENTER, x - LOGO_CENTER))
    }
}

/**
 * Animated brand intro displayed after Android's system launch splash.
 *
 * Rotation applies to the exterior badge only. The two gentle travelling
 * waves continuously morph the badge without replacing its original shape.
 */
@Composable
fun SongMeldSplashIntro(modifier: Modifier = Modifier) {
    val background = colorResource(R.color.songmeld_splash_background)
    val badgeColor = colorResource(R.color.songmeld_launcher_background)
    val perimeter = remember { sampleBadge() }
    val transition = rememberInfiniteTransition(label = "SongMeld intro")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Badge rotation"
    )
    val morphPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Travelling badge waves"
    )

    Box(
        modifier = modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(192.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val scale = size.minDimension / VIEWPORT_SIZE
                val center = Offset(size.width / 2f, size.height / 2f)
                val outline = Path()

                perimeter.forEachIndexed { index, sample ->
                    val dx = sample.position.x - LOGO_CENTER
                    val dy = sample.position.y - LOGO_CENTER
                    // A slight radial displacement preserves the approved
                    // silhouette instead of morphing into a generic star.
                    val displacement = 1f +
                        0.012f * sin(sample.angle * 6f + morphPhase) +
                        0.006f * sin(sample.angle * 9f - morphPhase * 1.3f)
                    val x = center.x + dx * displacement * scale
                    val y = center.y + dy * displacement * scale
                    if (index == 0) outline.moveTo(x, y) else outline.lineTo(x, y)
                }
                outline.close()
                rotate(degrees = rotation, pivot = center) {
                    drawPath(outline, color = badgeColor)
                }
            }

            // The center note stays perfectly still while the badge turns.
            Image(
                painter = painterResource(R.drawable.songmeld_splash_note),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
