package com.sun.englishlearning.screen.flashcard

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.max

class PageTransformer : ViewPager2.PageTransformer {
    
    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
    }
    
    override fun transformPage(view: View, position: Float) {
        view.apply {
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left
                    alpha = 0f
                    scaleX = MIN_SCALE
                    scaleY = MIN_SCALE
                }
                position <= 1 -> { // [-1,1]
                    // Card-like transformation
                    val scaleFactor = max(MIN_SCALE, 1 - abs(position))
                    val alphaFactor = max(MIN_ALPHA, 1 - abs(position))
                    
                    // Scale the page down (between MIN_SCALE and 1)
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                    
                    // Fade the page relative to its position
                    alpha = alphaFactor
                    
                    // Add subtle rotation for depth effect
                    rotationY = position * -10f
                    
                    // Add elevation effect
                    translationZ = (1 - abs(position)) * 10f
                    
                    // Subtle translation for smooth movement
                    translationX = position * -30f
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right
                    alpha = 0f
                    scaleX = MIN_SCALE
                    scaleY = MIN_SCALE
                }
            }
        }
    }
}
