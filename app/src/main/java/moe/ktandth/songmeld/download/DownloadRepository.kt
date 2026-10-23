package moe.ktandth.songmeld.download

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Initial single-session queue. No claim of persistence across process death is made here.
 * Each task keeps an immutable state snapshot; the Service owns actual download work.
 */
object DownloadRepository {
    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks = _tasks.asStateFlow()

    fun enqueue(preview: TrackPreview, formatId: String?): DownloadTask {
        val task = DownloadTask(
            id = UUID.randomUUID().toString(),
            sourceUrl = preview.sourceUrl,
            provider = preview.provider,
            title = preview.title,
            status = DownloadStatus.QUEUED,
            requestedFormatId = formatId,
        )
        _tasks.update { existing -> listOf(task) + existing }
        return task
    }

    fun task(id: String): DownloadTask? = _tasks.value.firstOrNull { it.id == id }

    fun update(id: String, transform: (DownloadTask) -> DownloadTask) {
        _tasks.update { tasks ->
            tasks.map { if (it.id == id) transform(it) else it }
        }
    }

    fun dismiss(id: String) {
        _tasks.update { tasks ->
            tasks.filterNot { it.id == id && !it.isActive }
        }
    }
}
