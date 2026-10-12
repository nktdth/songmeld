package moe.ktandth.songmeld

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import moe.ktandth.songmeld.ui.splash.SongMeldSplashIntro

private const val LEGACY_INTRO_DURATION_MS = 760L
private const val INTRO_FADE_MS = 180
private const val PLATFORM_MIN_VISIBLE_MS = 600L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val animate = savedInstanceState == null && ValueAnimator.areAnimatorsEnabled()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && animate) {
            // Android 12+ renders an AVD as soon as the platform splash appears.
            // Do not redraw it in Compose: that would jump back to zero rotation.
            splash.setOnExitAnimationListener { provider ->
                val animationStart = provider.iconAnimationStartMillis
                val elapsed = if (animationStart > 0L) {
                    (System.currentTimeMillis() - animationStart).coerceAtLeast(0L)
                } else {
                    0L
                }
                val remaining = (PLATFORM_MIN_VISIBLE_MS - elapsed).coerceAtLeast(0L)
                provider.view.animate()
                    .alpha(0f)
                    .setStartDelay(remaining)
                    .setDuration(INTRO_FADE_MS.toLong())
                    .withEndAction { provider.remove() }
                    .start()
            }
        }

        // Android 8-11 lack a system animated splash vector. Their first
        // visible logo frame is the Compose animation, not a static duplicate.
        setContent { SongMeldApp(playComposeIntro = animate && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) }
    }
}

@Composable
private fun SongMeldApp(playComposeIntro: Boolean) {
    var showIntro by remember { mutableStateOf(playComposeIntro) }

    LaunchedEffect(showIntro) {
        if (showIntro) {
            delay(LEGACY_INTRO_DURATION_MS)
            showIntro = false
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SongMeld",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

            AnimatedVisibility(
                visible = showIntro,
                enter = EnterTransition.None,
                exit = fadeOut(animationSpec = tween(INTRO_FADE_MS))
            ) {
                SongMeldSplashIntro()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SongMeldPreview() {
    SongMeldApp(playComposeIntro = false)
}
