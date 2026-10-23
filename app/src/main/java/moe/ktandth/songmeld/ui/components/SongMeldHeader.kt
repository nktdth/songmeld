package moe.ktandth.songmeld.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.settings.LocalDecorativeMotionEnabled
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import moe.ktandth.songmeld.ui.icons.SongMeldIcon

/**
 * Persistent app header, shared by the permission and Songs Library states.
 *
 * Place once above the state-dependent content. Keeping this composable mounted
 * prevents the branded entrance animation from restarting after permission changes.
 */
@Composable
fun SongMeldHeader(onOpenSettings: () -> Unit, modifier: Modifier = Modifier, animateEntrance: Boolean = true) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongMeldWordmark(animateEntrance = animateEntrance)

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(48.dp),
            ) {
                SongMeldIcon(
                    id = SongMeldIconId.MoreVert,
                    contentDescription = stringResource(R.string.menu_more_options),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_title)) },
                    leadingIcon = {
                        SongMeldIcon(id = SongMeldIconId.Settings, contentDescription = null)
                    },
                    onClick = {
                        menuExpanded = false
                        onOpenSettings()
                    },
                )
            }
        }
    }
}

@Composable
private fun SongMeldWordmark(modifier: Modifier = Modifier, animateEntrance: Boolean) {
    // Match the wordmark accent to the actual badge (including wallpaper colors).
    val badgeColor = colorResource(R.color.songmeld_launcher_background)
    val animationsEnabled = LocalDecorativeMotionEnabled.current && animateEntrance
    val textOffsetX = remember { Animatable(if (animationsEnabled) -36f else 0f) }
    val textAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val badgeAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val badgeScale = remember { Animatable(if (animationsEnabled) 0.82f else 1f) }

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) {
            textOffsetX.snapTo(0f)
            textAlpha.snapTo(1f)
            badgeAlpha.snapTo(1f)
            badgeScale.snapTo(1f)
            return@LaunchedEffect
        }
        launch {
            textOffsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
            )
        }
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            )
        }
        launch {
            delay(160)
            badgeAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            )
        }
        launch {
            delay(160)
            badgeScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            )
        }
    }

    Row(
        modifier = modifier
            .offset { IntOffset(textOffsetX.value.dp.roundToPx(), 0) }
            .alpha(textAlpha.value),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                ) {
                    append("Song")
                }
                withStyle(
                    style = SpanStyle(
                        color = badgeColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                ) {
                    append("Meld")
                }
            },
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.width(7.dp))

        Image(
            painter = painterResource(R.drawable.songmeld_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(22.dp)
                .alpha(badgeAlpha.value)
                .graphicsLayer {
                    scaleX = badgeScale.value
                    scaleY = badgeScale.value
                },
        )
    }
}
