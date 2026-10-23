package moe.ktandth.songmeld.ui.download

import android.content.ClipboardManager
import android.content.Context
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import moe.ktandth.songmeld.download.DownloadRepository
import moe.ktandth.songmeld.download.DownloadPreferences
import moe.ktandth.songmeld.download.DownloadStatus
import moe.ktandth.songmeld.download.DownloadTask
import moe.ktandth.songmeld.download.DownloadQualityPolicy
import moe.ktandth.songmeld.download.NewPipeAudioResolver
import moe.ktandth.songmeld.download.TrackPreview
import moe.ktandth.songmeld.download.detectDownloadProvider
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId

/**
 * Phase 1: link -> inspect source formats -> explicitly queue an audio-only download.
 * Search/album/playlist UI will be added only after the basic pipeline is verified.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddSongScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onQueue: (TrackPreview, String?) -> Unit,
    initialUrl: String = "",
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val downloadPreferences = remember(context) { DownloadPreferences(context.applicationContext) }
    var url by rememberSaveable { mutableStateOf(initialUrl) }
    var busy by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf<TrackPreview?>(null) }
    var selectedFormatId by remember { mutableStateOf<String?>(null) }
    val notifyPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission is optional: proceed even when the user declines notifications.
        preview?.let { onQueue(it, selectedFormatId) }
    }
    val provider = detectDownloadProvider(url)
    fun queueDownload() {
        val selectedPreview = preview ?: return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else onQueue(selectedPreview, selectedFormatId)
    }

    fun inspect() {
        val submitted = url.trim()
        if (busy || detectDownloadProvider(submitted) == null) return
        scope.launch {
            busy = true
            preview = null
            failure = null
            try {
                val result = NewPipeAudioResolver.preview(submitted)
                preview = result
                selectedFormatId = DownloadPreferences.chooseDefaultFormat(
                    result.formats, downloadPreferences.quality, downloadPreferences.preferredCodec
                )?.streamId
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                failure = error.message ?: "Không đọc được thông tin nguồn nhạc."
            } finally {
                busy = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm bài hát") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        SongMeldIcon(SongMeldIconId.ArrowBack, "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        SongMeldIcon(SongMeldIconId.Settings, "Cài đặt tải xuống")
                    }
                },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    "Dán liên kết YouTube, YouTube Music hoặc SoundCloud",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it; preview = null; failure = null },
                    label = { Text("Liên kết bài hát") },
                    supportingText = {
                        Text(
                            when {
                                url.isBlank() -> "Chỉ hỗ trợ tải từng bài ở giai đoạn đầu."
                                provider != null -> "Nguồn: " + provider.displayName
                                else -> "Liên kết không hợp lệ hoặc chưa được hỗ trợ."
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = url.isNotBlank() && provider == null,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(ClipboardManager::class.java)
                            val pasted = clipboard?.primaryClip?.takeIf { it.itemCount > 0 }
                                ?.getItemAt(0)?.coerceToText(context)?.toString()
                            if (!pasted.isNullOrBlank()) {
                                url = pasted.trim()
                                failure = null
                                preview = null
                            } else failure = "Bộ nhớ tạm không có liên kết."
                        }
                    ) {
                        SongMeldIcon(SongMeldIconId.Link, null, size = 18.dp)
                        Spacer(Modifier.size(6.dp))
                        Text("Dán")
                    }
                    Button(
                        enabled = provider != null && !busy,
                        onClick = ::inspect,
                    ) {
                        SongMeldIcon(SongMeldIconId.Search, null, size = 18.dp)
                        Spacer(Modifier.size(7.dp))
                        Text("Kiểm tra liên kết")
                    }
                }
            }
            if (busy) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(40.dp))
                        Text("Đang tìm thông tin âm thanh…", modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
            failure?.let { message ->
                item {
                    Card {
                        Text(
                            message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
            preview?.let { track ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(track.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                track.uploader.ifBlank { track.provider.displayName },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                track.provider.displayName + " · " +
                                    (track.durationSeconds / 60) + ":" +
                                    (track.durationSeconds % 60).toString().padStart(2, '0'),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
                item {
                    Text("Chọn âm thanh gốc", style = MaterialTheme.typography.titleMedium)
                }
                items(track.formats, key = { it.streamId }) { format ->
                    val selected = selectedFormatId == format.streamId
                    ListItem(
                        headlineContent = { Text(format.label) },
                        supportingContent = { Text(format.mimeType) },
                        leadingContent = {
                            RadioButton(selected = selected, onClick = null)
                        },
                        modifier = Modifier.selectable(
                            selected = selected,
                            onClick = { selectedFormatId = format.streamId },
                            role = Role.RadioButton,
                        ),
                    )
                }
                item {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = ::queueDownload,
                        enabled = !busy && selectedFormatId != null,
                    ) {
                        SongMeldIcon(SongMeldIconId.Download, null, size = 20.dp)
                        Spacer(Modifier.size(8.dp))
                        Text("Thêm vào hàng đợi")
                    }
                }
                item {
                    Text(
                        if (downloadPreferences.quality == DownloadQualityPolicy.ASK_EACH_TIME)
                            "Đang chọn thủ công. Hãy chọn một định dạng phía trên trước khi tải."
                        else "Giữ nguyên M4A, WebM/Opus hoặc định dạng nguồn; " +
                            "chưa chuyển mã MP3. Chỉ lưu nội dung anh có quyền tải.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadActivityScreen(
    onBack: () -> Unit,
    onAddSong: () -> Unit,
    onOpenSettings: () -> Unit,
    onCancel: (String) -> Unit,
    onRetry: (DownloadTask) -> Unit,
) {
    val tasks by DownloadRepository.tasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tải xuống") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        SongMeldIcon(SongMeldIconId.ArrowBack, "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        SongMeldIcon(SongMeldIconId.Settings, "Cài đặt tải xuống")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (tasks.isEmpty()) {
                item {
                    Text(
                        "Chưa có bản tải nào. Hãy thêm một liên kết để bắt đầu.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    TextButton(onClick = onAddSong) { Text("Thêm bài hát") }
                }
            }
            items(tasks, key = { it.id }) { task ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    task.provider.displayName + " · " + task.status.displayLabel(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (task.isActive) {
                                TextButton(onClick = { onCancel(task.id) }) { Text("Hủy") }
                            } else if (task.status == DownloadStatus.FAILED ||
                                task.status == DownloadStatus.CANCELED) {
                                TextButton(onClick = { onRetry(task) }) { Text("Thử lại") }
                            } else {
                                IconButton(onClick = { DownloadRepository.dismiss(task.id) }) {
                                    SongMeldIcon(SongMeldIconId.Close, "Xóa khỏi lịch sử")
                                }
                            }
                        }
                        if (task.isActive) {
                            val progress = task.progress
                            if (progress != null) {
                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                        task.error?.let { message ->
                            Text(message, color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun DownloadStatus.displayLabel(): String = when (this) {
    DownloadStatus.QUEUED -> "Đang chờ"
    DownloadStatus.PREPARING -> "Đang chuẩn bị"
    DownloadStatus.DOWNLOADING -> "Đang tải"
    DownloadStatus.SAVING -> "Đang lưu"
    DownloadStatus.COMPLETED -> "Đã hoàn tất"
    DownloadStatus.FAILED -> "Thất bại"
    DownloadStatus.CANCELED -> "Đã hủy"
}
