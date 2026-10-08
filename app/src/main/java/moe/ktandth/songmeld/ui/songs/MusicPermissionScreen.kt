package moe.ktandth.songmeld.ui.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.R

@Composable
fun MusicPermissionScreen(
    showSettingsAction: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PermissionHeader()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PermissionArtwork()
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.permission_music_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.permission_music_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = if (showSettingsAction) onOpenSettings else onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                ) {
                    Icon(
                        imageVector = if (showSettingsAction) Icons.Rounded.Settings else Icons.Rounded.FolderOpen,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(
                            if (showSettingsAction) R.string.permission_open_settings
                            else R.string.permission_allow_access
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.permission_privacy_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PermissionHeader() {
    Text(
        text = stringResource(R.string.songs_title),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun PermissionArtwork() {
    Box(
        modifier = Modifier.size(128.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(112.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.LibraryMusic,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            shadowElevation = 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
