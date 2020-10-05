package ru.skillbranch.skillarticles.ui.custom.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting

class InlineCodeSpan(
    @ColorInt
    private val textColor: Int,
    @ColorInt
    private val bgColor: Int,
    @Px
    private val cornerRadius: Float,
    @Px
    private val padding: Float
) : ReplacementSpan() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var rect: RectF = RectF()
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var measureWidth: Int = 0

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        paint.forText {
            val measuredText = paint.measureText(text.toString(), start, end)
            measureWidth = (measuredText + 2 * padding).toInt()
        }
        return measureWidth
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val b = y + paint.descent()
        paint.forBackground {
            rect.set(x, top.toFloat(), x + measureWidth, b)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }

        paint.forText {
            canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
        }
    }

    private inline fun Paint.forText(block: () -> Unit) {
        val oldSize = textSize
        val oldStyle = typeface?.style ?: 0
        val oldColor = color
        val oldTypeface = typeface

        color = textColor
        typeface = Typeface.create(Typeface.MONOSPACE, oldStyle)
        textSize *= 0.85f

        block.invoke()

        typeface = oldTypeface
        color = oldColor
        textSize = oldSize
    }

    private inline fun Paint.forBackground(block: () -> Unit) {
        val oldStyle = style
        val oldColor = color

        color = bgColor
        style = Paint.Style.FILL

        block.invoke()

        color = oldColor
        style = oldStyle
    }
}