package com.sun.englishlearning.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.io.IOException

class AudioManager private constructor() {

    companion object {
        private const val TAG = "AudioManager"
        
        @Volatile
        private var INSTANCE: AudioManager? = null
        
        fun getInstance(): AudioManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioManager().also { INSTANCE = it }
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isPlayingAudio = false
    private var currentAudioUrl: String? = null

    interface AudioPlaybackListener {
        fun onAudioStarted()
        fun onAudioCompleted()
        fun onAudioError(error: String)
    }

    /**
     * Play audio from URL
     */
    fun playAudio(
        context: Context,
        audioUrl: String,
        listener: AudioPlaybackListener? = null
    ) {
        if (audioUrl.isEmpty()) {
            listener?.onAudioError("No audio URL provided")
            Toast.makeText(context, "Audio not available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Stop current playback if any
            stopAudio()

            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener {
                    Log.d(TAG, "Audio prepared, starting playback")
                    start()
                    isPlayingAudio = true
                    currentAudioUrl = audioUrl
                    listener?.onAudioStarted()
                }

                setOnCompletionListener {
                    Log.d(TAG, "Audio playback completed")
                    isPlayingAudio = false
                    currentAudioUrl = null
                    listener?.onAudioCompleted()
                    release()
                    mediaPlayer = null
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    val errorMessage = "Failed to play audio"
                    listener?.onAudioError(errorMessage)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    stopAudio()
                    true
                }

                try {
                    setDataSource(context, Uri.parse(audioUrl))
                    prepareAsync()
                } catch (e: IOException) {
                    Log.e(TAG, "Error setting data source", e)
                    listener?.onAudioError("Failed to load audio")
                    Toast.makeText(context, "Failed to load audio", Toast.LENGTH_SHORT).show()
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Invalid audio URL", e)
                    listener?.onAudioError("Invalid audio URL")
                    Toast.makeText(context, "Invalid audio URL", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error creating MediaPlayer", e)
            listener?.onAudioError("Audio playback failed")
            Toast.makeText(context, "Audio playback failed", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Stop current audio playback
     */
    fun stopAudio() {
        try {
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "MediaPlayer was in invalid state when stopping", e)
                }
                player.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio", e)
        } finally {
            mediaPlayer = null
            isPlayingAudio = false
            currentAudioUrl = null
        }
    }

    /**
     * Check if audio is currently playing
     */
    fun isAudioPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true && isPlayingAudio
        } catch (e: Exception) {
            Log.e(TAG, "Error checking playback state", e)
            false
        }
    }

    /**
     * Pause current audio playback
     */
    fun pauseAudio() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    isPlayingAudio = false
                    Log.d(TAG, "Audio paused")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio", e)
        }
    }

    /**
     * Resume paused audio playback
     */
    fun resumeAudio() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    isPlayingAudio = true
                    Log.d(TAG, "Audio resumed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming audio", e)
        }
    }

    /**
     * Get current audio URL
     */
    fun getCurrentAudioUrl(): String? = currentAudioUrl

    /**
     * Release resources
     */
    fun release() {
        stopAudio()
    }
}
