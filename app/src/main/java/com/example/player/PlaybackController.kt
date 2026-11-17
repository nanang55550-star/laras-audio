package com.example.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.model.AudioTrack
import com.example.model.SleepTimerMode
import com.example.model.SleepTimerState
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackSnapshot(
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queueTrackIds: List<String> = emptyList(),
    val currentQueueIndex: Int = -1
)

class PlaybackController(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackSnapshot())
    val playbackState: StateFlow<PlaybackSnapshot> = _playbackState.asStateFlow()

    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    private var positionTickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncSnapshot()
            if (isPlaying) startPositionTicker() else stopPositionTicker()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val previousState = _sleepTimerState.value
            if (previousState.mode == SleepTimerMode.END_OF_TRACK && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                mediaController?.pause()
                _sleepTimerState.value = SleepTimerState()
            }
            syncSnapshot()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED && _sleepTimerState.value.mode == SleepTimerMode.END_OF_TRACK) {
                _sleepTimerState.value = SleepTimerState()
            }
            syncSnapshot()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            syncSnapshot()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            syncSnapshot()
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            syncSnapshot()
        }
    }

    fun connect() {
        if (controllerFuture != null) return
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener({
            runCatching {
                val controller = future.get()
                mediaController = controller
                controller.addListener(playerListener)
                syncSnapshot()
                if (controller.isPlaying) startPositionTicker()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun disconnect() {
        stopPositionTicker()
        mediaController?.removeListener(playerListener)
        mediaController = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }

    fun playQueue(tracks: List<AudioTrack>, startIndex: Int, shuffle: Boolean? = null, repeatMode: Int? = null) {
        if (tracks.isEmpty()) return
        val controller = mediaController ?: run {
            connect()
            return
        }
        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        val mediaItems = tracks.map { it.toMediaItem() }
        controller.volume = 1f
        controller.setMediaItems(mediaItems, safeIndex, 0L)
        shuffle?.let { controller.shuffleModeEnabled = it }
        repeatMode?.let { controller.repeatMode = it }
        controller.prepare()
        controller.play()
        syncSnapshot()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.volume = 1f
            if (controller.playbackState == Player.STATE_IDLE && controller.mediaItemCount > 0) {
                controller.prepare()
            }
            controller.play()
        }
        syncSnapshot()
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        val clamped = positionMs.coerceAtLeast(0L)
        controller.seekTo(clamped)
        _playbackState.update { it.copy(positionMs = clamped) }
    }

    fun skipNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else if (controller.mediaItemCount > 0) {
            controller.seekTo(0, 0L)
        }
        syncSnapshot()
    }

    fun skipPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3_500L) {
            controller.seekTo(0L)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else {
            controller.seekTo(0L)
        }
        syncSnapshot()
    }

    fun toggleShuffle(): Boolean {
        val controller = mediaController ?: return !_playbackState.value.shuffleEnabled
        val next = !controller.shuffleModeEnabled
        controller.shuffleModeEnabled = next
        syncSnapshot()
        return next
    }

    fun cycleRepeatMode(): Int {
        val current = mediaController?.repeatMode ?: _playbackState.value.repeatMode
        val next = when (current) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = next
        syncSnapshot()
        return next
    }

    fun setShuffleAndRepeat(shuffle: Boolean, repeatMode: Int) {
        mediaController?.let {
            it.shuffleModeEnabled = shuffle
            it.repeatMode = repeatMode
        }
        _playbackState.update { it.copy(shuffleEnabled = shuffle, repeatMode = repeatMode) }
    }

    fun jumpToQueueIndex(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.seekTo(index, 0L)
            controller.play()
            syncSnapshot()
        }
    }

    fun addTrackToQueue(track: AudioTrack) {
        val controller = mediaController ?: return
        if (controller.mediaItemCount == 0) {
            playQueue(listOf(track), 0)
        } else {
            controller.addMediaItem(track.toMediaItem())
            syncSnapshot()
        }
    }

    fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount && controller.mediaItemCount > 1) {
            controller.removeMediaItem(index)
            syncSnapshot()
        }
    }

    fun startSleepCountdown(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalMs = minutes.coerceAtLeast(1) * 60_000L
        mediaController?.volume = 1f
        _sleepTimerState.value = SleepTimerState(
            mode = SleepTimerMode.COUNTDOWN,
            remainingMs = totalMs,
            totalDurationMs = totalMs,
            isFadingOut = false
        )
        sleepTimerJob = scope.launch {
            var remaining = totalMs
            val stepMs = 500L
            while (isActive && remaining > 0L) {
                delay(stepMs)
                remaining = (remaining - stepMs).coerceAtLeast(0L)
                val fading = remaining in 1L..10_000L
                if (fading) {
                    val vol = (remaining / 10_000f).coerceIn(0.05f, 1f)
                    mediaController?.volume = vol
                }
                _sleepTimerState.value = SleepTimerState(
                    mode = SleepTimerMode.COUNTDOWN,
                    remainingMs = remaining,
                    totalDurationMs = totalMs,
                    isFadingOut = fading
                )
            }
            if (isActive) {
                mediaController?.pause()
                mediaController?.volume = 1f
                _sleepTimerState.value = SleepTimerState()
                syncSnapshot()
            }
        }
    }

    fun startSleepEndOfTrack() {
        sleepTimerJob?.cancel()
        mediaController?.volume = 1f
        _sleepTimerState.value = SleepTimerState(
            mode = SleepTimerMode.END_OF_TRACK,
            remainingMs = 0L,
            totalDurationMs = 0L,
            isFadingOut = false
        )
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        mediaController?.volume = 1f
        _sleepTimerState.value = SleepTimerState()
    }

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = scope.launch {
            while (isActive) {
                syncPositionOnly()
                delay(100L)
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    private fun syncPositionOnly() {
        val controller = mediaController ?: return
        val pos = controller.currentPosition.coerceAtLeast(0L)
        val dur = controller.duration.takeIf { it > 0L } ?: _playbackState.value.durationMs
        _playbackState.update { it.copy(positionMs = pos, durationMs = dur) }
    }

    private fun syncSnapshot() {
        val controller = mediaController ?: return
        val queueIds = (0 until controller.mediaItemCount).map { idx ->
            controller.getMediaItemAt(idx).mediaId
        }
        val currentIdx = controller.currentMediaItemIndex
        val currentId = controller.currentMediaItem?.mediaId
        val pos = controller.currentPosition.coerceAtLeast(0L)
        val dur = controller.duration.takeIf { it > 0L } ?: 0L
        _playbackState.value = PlaybackSnapshot(
            currentTrackId = currentId,
            isPlaying = controller.isPlaying,
            positionMs = pos,
            durationMs = dur,
            shuffleEnabled = controller.shuffleModeEnabled,
            repeatMode = controller.repeatMode,
            queueTrackIds = queueIds,
            currentQueueIndex = currentIdx
        )
    }

    private fun AudioTrack.toMediaItem(): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
        artworkUri?.let { metadataBuilder.setArtworkUri(Uri.parse(it)) }

        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(Uri.parse(uriString))
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }
}
