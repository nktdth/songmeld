package moe.ktandth.songmeld.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId

/**
 * The bottom-right Add FAB is inspired by SongMeld-v1; Google M3E now owns all
 * expansion/shape/spacing animations through its official FAB Menu components.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongMeldDownloadFab(
    onAddSong: () -> Unit,
    onDownloads: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    BackHandler(enabled = expanded) { expanded = false }
    // V1's plus icon rotates into an X; native M3E animates the menu items and FAB shape.
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 430f),
        label = "downloadFabPlusRotation",
    )
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 420f),
        label = "downloadFabScale",
    )
    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it },
                modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
            ) {
                SongMeldIcon(
                    id = SongMeldIconId.Add,
                    contentDescription = if (expanded) "Đóng menu" else "Mở menu thêm nhạc",
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                )
            }
        },
    ) {
        FloatingActionButtonMenuItem(
            onClick = { expanded = false; onAddSong() },
            icon = { SongMeldIcon(SongMeldIconId.Link, null) },
            text = { Text("Thêm từ liên kết") },
        )
        FloatingActionButtonMenuItem(
            onClick = { expanded = false; onDownloads() },
            icon = { SongMeldIcon(SongMeldIconId.Download, null) },
            text = { Text("Tải xuống") },
        )
    }
}
