package com.example.armeasure

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class MeasureOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // -------------------------
    // Paints
    // -------------------------

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(8f, 0f, 0f, Color.BLACK)
    }

    // -------------------------
    // Data
    // -------------------------

    private val points = mutableListOf<PointF>()
    private var previewStart: PointF? = null
    private var previewEnd: PointF? = null
    private var labelText: String? = null

    // -------------------------
    // Public API
    // -------------------------

    fun setMeasurement(pts: List<PointF>, label: String?) {
        points.clear()
        points.addAll(pts)
        labelText = label
        invalidate()
    }

    fun setPreview(start: PointF?, end: PointF?) {
        previewStart = start
        previewEnd = end
        invalidate()
    }

    fun clearPreview() {
        previewStart = null
        previewEnd = null
        invalidate()
    }

    // -------------------------
    // Drawing
    // -------------------------

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw fixed measurement line
        if (points.size == 2) {
            val a = points[0]
            val b = points[1]

            canvas.drawLine(a.x, a.y, b.x, b.y, linePaint)

            labelText?.let {
                drawLabel(canvas, a, b, it)
            }
        }

        // Draw preview line (only when 1 point exists)
        if (points.size == 1 && previewStart != null && previewEnd != null) {
            canvas.drawLine(
                previewStart!!.x,
                previewStart!!.y,
                previewEnd!!.x,
                previewEnd!!.y,
                linePaint
            )
        }

        // Draw red points
        for (p in points) {
            canvas.drawCircle(p.x, p.y, 14f, pointPaint)
        }
    }

    private fun drawLabel(canvas: Canvas, a: PointF, b: PointF, text: String) {
        val midX = (a.x + b.x) / 2f
        val midY = (a.y + b.y) / 2f

        val textWidth = textPaint.measureText(text)
        canvas.drawText(
            text,
            midX - textWidth / 2f,
            midY - 20f,
            textPaint
        )
    }
}