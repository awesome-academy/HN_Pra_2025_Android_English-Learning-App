package com.sun.englishlearning.utils

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import java.io.IOException

class AudioPlayer(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    
    fun playAudio(audioUrl: String) {
        try {
            // Stop any currently playing audio
            stopAudio()
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()
                
                setOnPreparedListener { mp ->
                    mp.start()
                }
                
                setOnCompletionListener {
                    stopAudio()
                }
                
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(context, "Audio playback failed", Toast.LENGTH_SHORT).show()
                    stopAudio()
                    true
                }
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Failed to load audio", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopAudio() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.stop()
            }
            mp.reset()
            mp.release()
        }
        mediaPlayer = null
    }
    
    fun release() {
        stopAudio()
    }
}
