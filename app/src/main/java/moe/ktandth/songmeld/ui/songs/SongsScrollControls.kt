package moe.ktandth.songmeld.ui.songs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.GenericShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.Normalizer
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.data.model.Song
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import moe.ktandth.songmeld.ui.settings.LocalDecorativeMotionEnabled

/** SongMeld-v1-inspired right-edge fast scroller. Only the 28dp edge lane handles dragging. */
@Composable
internal fun SongsFastScrollbar(
    listState: LazyListState,
    songs: List<Song>,
    sort: SongSort,
    modifier: Modifier = Modifier,
) {
    val visible by remember(listState) {
        derivedStateOf { listState.canScrollForward || listState.canScrollBackward }
    }
    if (!visible || songs.size < 12) return

    val layout = listState.layoutInfo
    val count = layout.totalItemsCount
    val visibleCount = layout.visibleItemsInfo.size
    val fractionVisible = if (count == 0) 1f
        else (visibleCount.toFloat() / count).coerceIn(0.08f, 1f)

    var dragging by remember { mutableStateOf(false) }
    var letter by remember { mutableStateOf<String?>(null) }
    val latestSongs by rememberUpdatedState(songs)
    val latestSort by rememberUpdatedState(sort)
    val latestCount by rememberUpdatedState(count)
    val latestVisibleCount by rememberUpdatedState(visibleCount)
    val density = LocalDensity.current
    val motion = LocalDecorativeMotionEnabled.current
    val colors = MaterialTheme.colorScheme

    BoxWithConstraints(
        modifier = modifier.width(76.dp).padding(vertical = 6.dp),
    ) {
        val trackHeight = maxHeight
        val thumbHeight = (trackHeight * fractionVisible)
            .coerceAtLeast(44.dp).coerceAtMost(trackHeight)
        val travel = (trackHeight - thumbHeight).coerceAtLeast(0.dp)
        val firstSize = layout.visibleItemsInfo
            .firstOrNull { it.index == listState.firstVisibleItemIndex }
            ?.size?.coerceAtLeast(1) ?: 1
        val fractionalIndex = listState.firstVisibleItemIndex +
            listState.firstVisibleItemScrollOffset.toFloat() / firstSize
        val maxIndex = (count - visibleCount).coerceAtLeast(1)
        val progress = (fractionalIndex / maxIndex).coerceIn(0f, 1f)
        val thumbOffset = travel * progress
        val popupHeight = 44.dp
        val popupOffset = (thumbOffset + (thumbHeight - popupHeight) / 2)
            .coerceIn(0.dp, (trackHeight - popupHeight).coerceAtLeast(0.dp))
        val latestTrackPx by rememberUpdatedState(with(density) { trackHeight.toPx() })
        val latestThumbPx by rememberUpdatedState(with(density) { thumbHeight.toPx() })

        // Unlike the visual popup, only the narrow track catches gestures.
        Box(
            modifier = Modifier.align(Alignment.CenterEnd).width(28.dp).fillMaxHeight()
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
                    setProgress { next ->
                        val maxTarget = (latestCount - latestVisibleCount).coerceAtLeast(0)
                        listState.requestScrollToItem(
                            (next.coerceIn(0f, 1f) * maxTarget).roundToInt()
                        )
                        true
                    }
                }
                .pointerInput(listState) {
                    fun scrub(positionY: Float) {
                        val travelPx = (latestTrackPx - latestThumbPx).coerceAtLeast(1f)
                        val next = ((positionY - latestThumbPx / 2f) / travelPx).coerceIn(0f, 1f)
                        val currentSongs = latestSongs
                        val maxTarget = (latestCount - latestVisibleCount).coerceAtLeast(0)
                        val target = (next * maxTarget).roundToInt().coerceIn(0, maxTarget)
                        if (latestSort == SongSort.TITLE && currentSongs.isNotEmpty()) {
                            letter = fastScrollSection(
                                currentSongs[target.coerceAtMost(currentSongs.lastIndex)].title
                            )
                        } else {
                            letter = null
                        }
                        listState.requestScrollToItem(target)
                    }
                    detectDragGestures(
                        onDragStart = { dragging = true; scrub(it.y) },
                        onDragEnd = { dragging = false; letter = null },
                        onDragCancel = { dragging = false; letter = null },
                        onDrag = { change, _ -> change.consume(); scrub(change.position.y) },
                    )
                },
        ) {
            Box(
                Modifier.align(Alignment.CenterEnd).width(6.dp).fillMaxHeight()
                    .clip(CircleShape).background(colors.primary.copy(alpha = 0.12f))
            )
            Box(
                Modifier.align(Alignment.TopEnd).offset(y = thumbOffset)
                    .width(6.dp).height(thumbHeight).clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.9f))
            )
        }

        val showLetter = dragging && sort == SongSort.TITLE && letter != null
        if (motion) {
            AnimatedVisibility(
                visible = showLetter,
                modifier = Modifier.align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = popupOffset),
                enter = fadeIn(tween(110)) + scaleIn(initialScale = 0.94f, animationSpec = tween(130)),
                exit = fadeOut(tween(90)) + scaleOut(targetScale = 0.96f, animationSpec = tween(110)),
            ) {
                FastScrollLetter(letter.orEmpty())
            }
        } else if (showLetter) {
            Box(Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = popupOffset)) {
                FastScrollLetter(letter.orEmpty())
            }
        }
    }
}

/** Vietnamese Đ and combining accents are grouped under their Latin fast-scroll letter. */
internal fun fastScrollSection(title: String): String {
    val initial = title.trim().firstOrNull() ?: return "#"
    val normalized = if (initial == 'Đ' || initial == 'đ') "D" else
        Normalizer.normalize(initial.toString(), Normalizer.Form.NFD)
    val capital = normalized.firstOrNull()?.uppercaseChar() ?: return "#"
    return if (capital in 'A'..'Z') capital.toString() else "#"
}

@Composable
private fun FastScrollLetter(label: String) {
    Surface(
        modifier = Modifier.width(78.dp).height(44.dp),
        shape = FastScrollBubbleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        Box(Modifier.padding(start = 12.dp, end = 27.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private val FastScrollBubbleShape = GenericShape { size, _ ->
    // Rounded, right-pointing bubble based on SongMeld-v1's fast-scroll silhouette.
    val h = size.height
    val r = h / 2f
    val x = size.width
    moveTo(r, 0f)
    lineTo(x * 0.56f, 0f)
    cubicTo(x * 0.75f, 0f, x * 0.80f, h * 0.22f, x * 0.96f, h * 0.44f)
    quadraticTo(x, h * 0.5f, x * 0.96f, h * 0.56f)
    cubicTo(x * 0.80f, h * 0.78f, x * 0.75f, h, x * 0.56f, h)
    lineTo(r, h)
    cubicTo(r * 0.45f, h, 0f, h * 0.78f, 0f, r)
    cubicTo(0f, h * 0.22f, r * 0.45f, 0f, r, 0f)
    close()
}

/** The old v1 scroll-to-top FAB without the unimplemented Add Songs menu. */
@Composable
internal fun SongsScrollToTopFab(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val visible by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 1 ||
                (listState.firstVisibleItemIndex > 0 && listState.firstVisibleItemScrollOffset > 0)
        }
    }
    var expanded by remember { mutableStateOf(false) }
    val motion = LocalDecorativeMotionEnabled.current
    val scope = rememberCoroutineScope()

    // The label expands when users really scroll upward and shrinks when going down.
    // Different thresholds avoid the tiny-gesture flicker seen in v1.
    LaunchedEffect(listState, motion) {
        if (!motion) return@LaunchedEffect
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset
        var upward = 0
        var downward = 0
        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                listState.isScrollInProgress,
            )
        }.collect { (index, offset, scrolling) ->
            if (!scrolling) {
                upward = 0
                downward = 0
            } else {
                val delta = when {
                    index > previousIndex -> 800
                    index < previousIndex -> -800
                    else -> offset - previousOffset
                }
                when {
                    delta < 0 -> {
                        upward -= delta
                        downward = 0
                        if (upward >= 18) { expanded = true; upward = 0 }
                    }
                    delta > 0 -> {
                        downward += delta
                        upward = 0
                        if (downward >= 48) { expanded = false; downward = 0 }
                    }
                }
            }
            previousIndex = index
            previousOffset = offset
        }
    }
    LaunchedEffect(visible) { if (!visible) expanded = false }

    val fab: @Composable () -> Unit = {
        FloatingActionButton(
            onClick = { scope.launch { listState.animateScrollToItem(0) } },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SongMeldIcon(
                    SongMeldIconId.ArrowUpward,
                    contentDescription = stringResource(R.string.songs_scroll_to_top),
                    size = 24.dp,
                )
                if (motion) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandHorizontally(tween(170)),
                        exit = shrinkHorizontally(tween(150)),
                    ) {
                        Text(stringResource(R.string.songs_scroll_top_label),
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
    if (motion) {
        AnimatedVisibility(
            visible = visible,
            modifier = modifier,
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.9f, animationSpec = tween(180)),
            exit = fadeOut(tween(110)) + scaleOut(targetScale = 0.9f, animationSpec = tween(140)),
        ) { fab() }
    } else if (visible) {
        Box(modifier) { fab() }
    }
}
