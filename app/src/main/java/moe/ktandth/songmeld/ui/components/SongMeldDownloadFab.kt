package moe.ktandth.songmeld.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId

/**
 * Port of SongMeld-v1 SongsActionFab's actual composition/layout and animation curves.
 *
 * IMPORTANT: The bottom row belongs INSIDE the same Column as the expanding actions.
 * Keeping the scroll button outside beside FloatingActionButtonMenu made the FAB move
 * horizontally as that menu measured its wider labels (see visual regression #9).
 *
 * The original M3 FAB/ExtendedFAB defaults provide the native container/shape/elevation.
 */
@Composable
fun SongMeldDownloadFab(
    onAddSong: () -> Unit,
    onDownloads: () -> Unit,
    modifier: Modifier = Modifier,
    scrollAction: @Composable () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }
    BackHandler(enabled = menuExpanded) { menuExpanded = false }

    // Exact SongMeld-v1 values: 1.06f scale with 0.58/420 spring;
    // the + rotates to X by 45° using the 0.62/430 spring.
    val mainFabScale by animateFloatAsState(
        targetValue = if (menuExpanded) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 420f),
        label = "songsMainFabScale",
    )
    val addIconRotation by animateFloatAsState(
        targetValue = if (menuExpanded) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 430f),
        label = "songsAddIconRotation",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // This is SongMeld-v1's actual AnimatedVisibility motion, including the exit
        // timings. It is intentionally not the different M3E FAB Menu transition.
        AnimatedVisibility(
            visible = menuExpanded,
            enter = slideInVertically(
                animationSpec = spring(dampingRatio = 0.62f, stiffness = 430f),
                initialOffsetY = { it / 4 },
            ) + scaleIn(
                initialScale = 0.78f,
                animationSpec = spring(dampingRatio = 0.58f, stiffness = 420f),
            ),
            exit = fadeOut(animationSpec = tween(120)) +
                slideOutVertically(
                    animationSpec = tween(160),
                    targetOffsetY = { it / 4 },
                ) +
                scaleOut(animationSpec = tween(150), targetScale = 0.82f),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        menuExpanded = false
                        onAddSong()
                    },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 6.dp,
                        focusedElevation = 6.dp,
                        hoveredElevation = 6.dp,
                    ),
                    icon = { SongMeldIcon(SongMeldIconId.Link, contentDescription = null) },
                    text = { Text("Thêm từ liên kết") },
                )
                ExtendedFloatingActionButton(
                    onClick = {
                        menuExpanded = false
                        onDownloads()
                    },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 6.dp,
                        focusedElevation = 6.dp,
                        hoveredElevation = 6.dp,
                    ),
                    icon = { SongMeldIcon(SongMeldIconId.Download, contentDescription = null) },
                    text = { Text("Tải xuống") },
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Shares a fixed bottom row with the Add FAB; never shifts when menu opens.
            scrollAction()
            FloatingActionButton(
                onClick = { menuExpanded = !menuExpanded },
                modifier = Modifier.graphicsLayer {
                    scaleX = mainFabScale
                    scaleY = mainFabScale
                },
            ) {
                SongMeldIcon(
                    id = SongMeldIconId.Add,
                    contentDescription = if (menuExpanded) "Đóng menu thêm nhạc" else "Mở menu thêm nhạc",
                    modifier = Modifier.graphicsLayer { rotationZ = addIconRotation },
                )
            }
        }
    }
}
