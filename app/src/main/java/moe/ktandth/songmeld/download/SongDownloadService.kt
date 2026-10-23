package moe.ktandth.songmeld.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import moe.ktandth.songmeld.MainActivity
import moe.ktandth.songmeld.R

/**
 * Foreground sequential download worker for the first integration.
 * Starting from a user tap satisfies Android foreground-service launch rules.
 * Persistence, playlist batching, and resumable transfers are separate milestones.
 */
class SongDownloadService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pending = ArrayDeque<String>()
    private var activeId: String? = null
    private var activeJob: Job? = null
    private lateinit var manager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Tải nhạc SongMeld", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground within the allowed window, before any slow extraction work.
        startForeground(NOTIFICATION_ID, notification("Đang chuẩn bị tải nhạc", null))
        when (intent?.action) {
            ACTION_ENQUEUE -> {
                intent.getStringExtra(EXTRA_ID)?.let { id ->
                    if (DownloadRepository.task(id)?.status == DownloadStatus.QUEUED &&
                        id != activeId && id !in pending
                    ) pending.addLast(id)
                }
                startNext()
            }
            ACTION_CANCEL -> intent.getStringExtra(EXTRA_ID)?.let { id ->
                if (activeId == id) {
                    activeJob?.cancel()
                } else {
                    pending.remove(id)
                    DownloadRepository.update(id) {
                        it.copy(status = DownloadStatus.CANCELED)
                    }
                    startNext()
                }
            }
            else -> startNext()
        }
        return START_NOT_STICKY
    }

    private fun startNext() {
        if (activeId != null) return
        val id = pending.removeFirstOrNull()
        if (id == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        val task = DownloadRepository.task(id)
        if (task == null || task.status != DownloadStatus.QUEUED) {
            startNext()
            return
        }
        activeId = id
        activeJob = scope.launch {
            DownloadRepository.update(id) { it.copy(status = DownloadStatus.PREPARING) }
            try {
                updateNotification("Đang đọc thông tin bài hát", null)
                // Resolve a fresh media URL here because signed URLs can expire between preview
                // and queue execution.
                val preview = NewPipeAudioResolver.preview(task.sourceUrl)
                val audio = NewPipeAudioResolver.resolve(preview, task.requestedFormatId)
                DownloadRepository.update(id) {
                    it.copy(title = audio.title, status = DownloadStatus.DOWNLOADING)
                }
                updateNotification(audio.title, null)
                AudioFileSaver.save(applicationContext, audio) { bytes, total ->
                    val progress = total?.takeIf { it > 0 }?.let { bytes.toFloat() / it }
                    DownloadRepository.update(id) {
                        it.copy(status = DownloadStatus.DOWNLOADING,
                            downloadedBytes = bytes, totalBytes = total, progress = progress)
                    }
                    updateNotification(audio.title, progress)
                }
                DownloadRepository.update(id) {
                    it.copy(status = DownloadStatus.COMPLETED, progress = 1f, error = null)
                }
            } catch (_: CancellationException) {
                DownloadRepository.update(id) { it.copy(status = DownloadStatus.CANCELED) }
            } catch (failure: Exception) {
                DownloadRepository.update(id) {
                    it.copy(status = DownloadStatus.FAILED,
                        error = failure.message?.take(240) ?: "Không thể tải bài hát.")
                }
            } finally {
                // Service state is touched only from the main dispatcher to serialize intents.
                kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable + Dispatchers.Main) {
                    activeId = null
                    activeJob = null
                    startNext()
                }
            }
        }
    }

    private fun notification(title: String, progress: Float?): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SongMeld · Tải xuống")
            .setContentText(title)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        if (progress != null) {
            builder.setProgress(100, (progress.coerceIn(0f, 1f) * 100).toInt(), false)
        } else {
            builder.setProgress(0, 0, true)
        }
        return builder.build()
    }

    private fun updateNotification(title: String, progress: Float?) {
        manager.notify(NOTIFICATION_ID, notification(title, progress))
    }

    override fun onDestroy() {
        activeJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "songmeld_download"
        private const val NOTIFICATION_ID = 30421
        private const val ACTION_ENQUEUE = "moe.ktandth.songmeld.download.ENQUEUE"
        private const val ACTION_CANCEL = "moe.ktandth.songmeld.download.CANCEL"
        private const val EXTRA_ID = "task_id"

        fun enqueue(context: Context, id: String) {
            ContextCompat.startForegroundService(context, Intent(context, SongDownloadService::class.java).apply {
                action = ACTION_ENQUEUE
                putExtra(EXTRA_ID, id)
            })
        }

        fun cancel(context: Context, id: String) {
            context.startService(Intent(context, SongDownloadService::class.java).apply {
                action = ACTION_CANCEL
                putExtra(EXTRA_ID, id)
            })
        }
    }
}
