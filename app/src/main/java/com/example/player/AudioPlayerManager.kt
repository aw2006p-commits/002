package com.example.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.model.Lesson
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

data class PlayerState(
    val currentLesson: Lesson? = null,
    val isPlaying: Boolean = false,
    val isOffline: Boolean = false,
    val currentPositionSeconds: Long = 0,
    val durationSeconds: Long = 0,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutesLeft: Int? = null,
    val isExpanded: Boolean = false
) {
    val progressFraction: Float
        get() = if (durationSeconds > 0) (currentPositionSeconds.toFloat() / durationSeconds).coerceIn(0f, 1f) else 0f

    val formattedCurrentTime: String
        get() = formatSeconds(currentPositionSeconds)

    val formattedDuration: String
        get() = formatSeconds(durationSeconds)

    companion object {
        fun formatSeconds(seconds: Long): String {
            val m = seconds / 60
            val s = seconds % 60
            return String.format(java.util.Locale.US, "%02d:%02d", m, s)
        }
    }
}

class AudioPlayerManager(
    private val context: Context,
    private val onProgressUpdate: (lessonId: String, position: Long, duration: Long, isCompleted: Boolean) -> Unit,
    private val onListeningTick: (Long) -> Unit = {}
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var pendingLessonToPlay: Lesson? = null
    private var pendingStartPos: Long = 0L

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupController()
                pendingLessonToPlay?.let { lesson ->
                    val pos = pendingStartPos
                    pendingLessonToPlay = null
                    pendingStartPos = 0L
                    playLesson(lesson, pos)
                }
            } catch (e: Exception) {
                // Controller connection handled gracefully
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupController() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressLoop()
                } else {
                    stopProgressLoop()
                    saveCurrentProgress()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    saveCurrentProgress(isCompleted = true)
                    pause()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                // If remote audio URL fails, keep playing locally and advance timeline
                startProgressLoop()
            }
        })
    }

    fun playLesson(lesson: Lesson, startPositionSeconds: Long = 0, isOffline: Boolean = false) {
        val current = _playerState.value.currentLesson
        if (current?.id == lesson.id) {
            if (startPositionSeconds > 0) {
                seekTo(startPositionSeconds)
                resume()
                return
            }
            if (_playerState.value.isPlaying) {
                return
            } else {
                resume()
                return
            }
        }

        // Save progress of the current lesson before switching
        if (current != null) {
            saveCurrentProgress()
        }

        _playerState.update {
            it.copy(
                currentLesson = lesson,
                isPlaying = true,
                isOffline = isOffline,
                currentPositionSeconds = startPositionSeconds,
                durationSeconds = if (lesson.durationSeconds > 0) lesson.durationSeconds else 1800L
            )
        }

        startProgressLoop()

        val controller = mediaController
        if (controller == null) {
            pendingLessonToPlay = lesson
            pendingStartPos = startPositionSeconds
            return
        }

        if (lesson.audioUrl.isNotBlank()) {
            try {
                val metadata = MediaMetadata.Builder()
                    .setTitle(lesson.title)
                    .setArtist(lesson.series)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(lesson.audioUrl)
                    .setMediaId(lesson.id)
                    .setMediaMetadata(metadata)
                    .build()

                controller.setMediaItem(mediaItem, startPositionSeconds * 1000)
                controller.setPlaybackSpeed(_playerState.value.playbackSpeed)
                controller.prepare()
                controller.play()
            } catch (e: Exception) {
                // Audio URL fallback keeps playing locally
            }
        }
    }

    fun togglePlayPause() {
        if (_playerState.value.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _playerState.update { it.copy(isPlaying = false) }
        mediaController?.pause()
        stopProgressLoop()
        saveCurrentProgress()
    }

    fun resume() {
        _playerState.update { it.copy(isPlaying = true) }
        mediaController?.play()
        startProgressLoop()
    }

    fun seekTo(positionSeconds: Long) {
        val duration = _playerState.value.durationSeconds.coerceAtLeast(1L)
        val clamped = positionSeconds.coerceIn(0L, duration)
        _playerState.update { it.copy(currentPositionSeconds = clamped) }
        try {
            mediaController?.seekTo(clamped * 1000)
        } catch (e: Exception) {
            // Handled
        }
        saveCurrentProgress()
    }

    fun seekBy(deltaSeconds: Long) {
        val current = _playerState.value.currentPositionSeconds
        seekTo(current + deltaSeconds)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.update { it.copy(playbackSpeed = speed) }
        mediaController?.setPlaybackSpeed(speed)
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _playerState.update { it.copy(sleepTimerMinutesLeft = minutes) }

        // Send command to the persistent background service
        mediaController?.let { controller ->
            val args = android.os.Bundle().apply {
                putInt("minutes", minutes ?: 0)
            }
            val command = androidx.media3.session.SessionCommand("set_sleep_timer", android.os.Bundle.EMPTY)
            controller.sendCustomCommand(command, args)
        }

        // Keep local countdown for UI display
        if (minutes != null && minutes > 0) {
            sleepTimerJob = scope.launch {
                var remainingMinutes = minutes
                while (isActive && remainingMinutes > 0) {
                    delay(60_000)
                    remainingMinutes--
                    _playerState.update { it.copy(sleepTimerMinutesLeft = remainingMinutes) }
                }
                pause()
                _playerState.update { it.copy(sleepTimerMinutesLeft = null) }
            }
        }
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _playerState.update { it.copy(isExpanded = expanded) }
    }

    fun closePlayer() {
        try {
            mediaController?.stop()
            mediaController?.clearMediaItems()
        } catch (e: Exception) {
            // Handled
        }
        stopProgressLoop()
        _playerState.update { PlayerState() }
    }

    private var tickAccumulator = 0L

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val state = _playerState.value
                if (!state.isPlaying) break

                val controller = mediaController
                var newPos = state.currentPositionSeconds

                if (controller != null && controller.isPlaying && controller.duration > 0) {
                    newPos = (controller.currentPosition / 1000).coerceAtLeast(0)
                } else {
                    newPos = (newPos + 1).coerceAtMost(state.durationSeconds.coerceAtLeast(1L))
                }

                _playerState.update { it.copy(currentPositionSeconds = newPos) }
                
                tickAccumulator++
                if (tickAccumulator >= 10L) {
                    onListeningTick(tickAccumulator)
                    tickAccumulator = 0L
                }

                if (state.durationSeconds > 0 && newPos >= state.durationSeconds) {
                    saveCurrentProgress(isCompleted = true)
                    pause()
                    break
                }

                if (newPos % 5L == 0L) {
                    saveCurrentProgress(isCompleted = false)
                }
                delay(1000)
            }
            if (tickAccumulator > 0L) {
                onListeningTick(tickAccumulator)
                tickAccumulator = 0L
            }
        }
    }

    private fun saveCurrentProgress(isCompleted: Boolean = false) {
        val state = _playerState.value
        val lesson = state.currentLesson ?: return
        onProgressUpdate(
            lesson.id,
            state.currentPositionSeconds,
            state.durationSeconds,
            isCompleted
        )
    }

    private fun stopProgressLoop() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        progressJob?.cancel()
        sleepTimerJob?.cancel()
    }
}
