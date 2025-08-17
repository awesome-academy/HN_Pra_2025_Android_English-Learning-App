package com.sun.englishlearning.screen.me

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.sun.englishlearning.R

class CircleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var progress: Float = 0f // 0..1
    private var text: String = ""
    private var circleColor: Int = Color.LTGRAY
    private var progressColor: Int = Color.BLUE
    private var textColor: Int = Color.BLACK
    private var strokeWidth: Float = 16f

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 48f
    }

    init {
        // Có thể lấy thuộc tính từ XML nếu cần
        circleColor = ContextCompat.getColor(context, R.color.gray_dark)
        progressColor = ContextCompat.getColor(context, R.color.main_blue)
        textColor = ContextCompat.getColor(context, R.color.black)
    }

    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 1f)
        invalidate()
    }

    fun setText(text: String) {
        this.text = text
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = width.coerceAtMost(height)
        val radius = size / 2f - strokeWidth
        val cx = width / 2f
        val cy = height / 2f
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // Draw background circle
        paintCircle.color = circleColor
        paintCircle.strokeWidth = strokeWidth
        canvas.drawArc(rect, 0f, 360f, false, paintCircle)

        // Draw progress arc
        paintProgress.color = progressColor
        paintProgress.strokeWidth = strokeWidth
        canvas.drawArc(rect, -90f, 360f * progress, false, paintProgress)

        // Draw text in center
        paintText.color = textColor
        paintText.textSize = size / 4f
        val textY = cy - (paintText.descent() + paintText.ascent()) / 2
        canvas.drawText(text, cx, textY, paintText)
    }
}
