package com.example.player

import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var sleepTimerJob: Job? = null

    companion object {
        const val COMMAND_SET_SLEEP_TIMER = "set_sleep_timer"
        const val EXTRA_MINUTES = "minutes"
    }

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()
            
        val callback = object : MediaSession.Callback {
            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                if (customCommand.customAction == COMMAND_SET_SLEEP_TIMER) {
                    val minutes = args.getInt(EXTRA_MINUTES, 0)
                    handleSleepTimer(minutes)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                return super.onCustomCommand(session, controller, customCommand, args)
            }
        }
            
        mediaSession = MediaSession.Builder(this, exoPlayer!!)
            .setCallback(callback)
            .build()
    }

    private fun handleSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes > 0) {
            sleepTimerJob = serviceScope.launch {
                delay(minutes * 60_000L)
                if (isActive) {
                    exoPlayer?.pause()
                }
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        sleepTimerJob?.cancel()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
