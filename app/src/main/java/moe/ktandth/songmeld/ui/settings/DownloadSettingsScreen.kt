package moe.ktandth.songmeld.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.download.DownloadFolderPolicy
import moe.ktandth.songmeld.download.DownloadPreferences
import moe.ktandth.songmeld.download.DownloadQualityPolicy
import moe.ktandth.songmeld.download.PreferredAudioCodec

/**
 * The first download settings page deliberately contains only working switches and choices.
 * It is based on SongMeld-v1 and Seal, but uses official Compose Material 3 controls.
 */
@Composable
fun DownloadSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) { DownloadPreferences(context.applicationContext) }
    var quality by remember { mutableStateOf(preferences.quality) }
    var codec by remember { mutableStateOf(preferences.preferredCodec) }
    var folder by remember { mutableStateOf(preferences.folderPolicy) }
    var wifiOnly by remember { mutableStateOf(preferences.wifiOnly) }
    var notifyCompletion by remember { mutableStateOf(preferences.completionNotifications) }

    SettingsPageScaffold(title = R.string.download_settings_title, onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(stringResource(R.string.download_settings_audio))
            Text(
                stringResource(R.string.download_settings_quality),
                style = MaterialTheme.typography.titleMedium,
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = DownloadQualityPolicy.entries
                options.forEachIndexed { index, choice ->
                    SegmentedButton(
                        selected = quality == choice,
                        onClick = { quality = choice; preferences.quality = choice },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        label = {
                            Text(
                                when (choice) {
                                    DownloadQualityPolicy.BEST -> stringResource(R.string.download_quality_best)
                                    DownloadQualityPolicy.SAVE_DATA -> stringResource(R.string.download_quality_saver)
                                    DownloadQualityPolicy.ASK_EACH_TIME -> stringResource(R.string.download_quality_ask)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
            Text(
                stringResource(R.string.download_settings_quality_note),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                stringResource(R.string.download_settings_codec),
                style = MaterialTheme.typography.titleMedium,
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = PreferredAudioCodec.entries
                options.forEachIndexed { index, choice ->
                    SegmentedButton(
                        selected = codec == choice,
                        onClick = { codec = choice; preferences.preferredCodec = choice },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        label = {
                            Text(
                                when (choice) {
                                    PreferredAudioCodec.AUTO -> stringResource(R.string.download_codec_auto)
                                    PreferredAudioCodec.M4A_AAC -> "M4A/AAC"
                                    PreferredAudioCodec.OPUS_WEBM -> "Opus"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
            Text(
                stringResource(R.string.download_settings_codec_note),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.download_settings_storage))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column {
                    listOf(DownloadFolderPolicy.SINGLE, DownloadFolderPolicy.BY_SOURCE).forEach { choice ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    stringResource(
                                        if (choice == DownloadFolderPolicy.SINGLE)
                                            R.string.download_folder_single
                                        else R.string.download_folder_sources
                                    )
                                )
                            },
                            supportingContent = {
                                Text(
                                    if (choice == DownloadFolderPolicy.SINGLE)
                                        "Music/SongMeld"
                                    else "Music/SongMeld/YouTube, SoundCloud…"
                                )
                            },
                            trailingContent = {
                                RadioButton(selected = folder == choice, onClick = null)
                            },
                            modifier = Modifier.selectable(
                                selected = folder == choice,
                                role = Role.RadioButton,
                                onClick = { folder = choice; preferences.folderPolicy = choice },
                            ),
                            colors = androidx.compose.material3.ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.download_settings_storage_note),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.download_settings_behavior))
            SwitchPreference(
                title = stringResource(R.string.download_wifi_only),
                description = stringResource(R.string.download_wifi_only_description),
                checked = wifiOnly,
                onCheckedChange = { wifiOnly = it; preferences.wifiOnly = it },
            )
            SwitchPreference(
                title = stringResource(R.string.download_notify_completion),
                description = stringResource(R.string.download_notify_completion_description),
                checked = notifyCompletion,
                onCheckedChange = { notifyCompletion = it; preferences.completionNotifications = it },
            )
            Text(
                stringResource(R.string.download_settings_limits),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 6.dp),
    )
}

@Composable
private fun SwitchPreference(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}
