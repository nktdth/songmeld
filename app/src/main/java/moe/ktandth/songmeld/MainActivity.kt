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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import moe.ktandth.songmeld.ui.SongMeldApp
import moe.ktandth.songmeld.ui.splash.SongMeldSplashIntro
import moe.ktandth.songmeld.ui.theme.SongMeldTheme

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
            // Android 12+: animate the badge in the platform splash; do not
            // restart it in Compose or the rotation will visibly jump.
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

        setContent {
            SongMeldTheme {
                // The feature branch's real Songs Library remains the app root.
                // Legacy devices render the animated badge over that screen.
                SongMeldRoot(
                    playComposeIntro = animate && Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                )
            }
        }
    }
}

@Composable
private fun SongMeldRoot(playComposeIntro: Boolean) {
    var showIntro by remember { mutableStateOf(playComposeIntro) }

    LaunchedEffect(showIntro) {
        if (showIntro) {
            delay(LEGACY_INTRO_DURATION_MS)
            showIntro = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SongMeldApp()

        AnimatedVisibility(
            visible = showIntro,
            enter = EnterTransition.None,
            exit = fadeOut(animationSpec = tween(INTRO_FADE_MS))
        ) {
            SongMeldSplashIntro()
        }
    }
}
