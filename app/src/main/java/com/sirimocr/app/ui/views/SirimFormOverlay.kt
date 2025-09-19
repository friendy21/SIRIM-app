package com.sirimocr.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.sirimocr.app.R

class SirimFormOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val borderPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.overlay_guide)
        style = Paint.Style.STROKE
        strokeWidth = 6f
        pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.overlay_text)
        textSize = 40f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val left = width * 0.1f
        val top = height * 0.2f
        val right = width * 0.9f
        val bottom = height * 0.8f
        canvas.drawRoundRect(RectF(left, top, right, bottom), 24f, 24f, borderPaint)
        val rows = 7
        val rowHeight = (bottom - top) / rows
        for (i in 1 until rows) {
            val y = top + rowHeight * i
            canvas.drawLine(left, y, right, y, borderPaint)
        }
        val text = context.getString(R.string.permission_camera_rationale)
        val textWidth = textPaint.measureText(text)
        canvas.drawText(text, (width - textWidth) / 2f, top - 24f, textPaint)
    }
}
