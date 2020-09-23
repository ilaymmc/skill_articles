package ru.skillbranch.skillarticles.ui.custom.spans

import android.graphics.*
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import ru.skillbranch.skillarticles.data.repositories.Element


class BlockCodeSpan(
    @ColorInt
    private val textColor: Int,
    @ColorInt
    private val bgColor: Int,
    @Px
    private val cornerRadius: Float,
    @Px
    private val padding: Float,
    private val type: Element.BlockCode.Type
) : ReplacementSpan() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var rect = RectF()
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var path = Path()

//    private var measureWidth: Int = 0

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
        paint.forBackground {
//            rect.set(x, top.toFloat(), x + measureWidth, bottom.toFloat())
            val topPadding =
                if (type == Element.BlockCode.Type.SINGLE || type == Element.BlockCode.Type.START)
                    padding else 0f
            val bottomPadding =
                if (type == Element.BlockCode.Type.SINGLE || type == Element.BlockCode.Type.END)
                    padding else 0f
            rect.set(0f, top + topPadding, canvas.width.toFloat(), bottom - bottomPadding)
            when(type) {
                Element.BlockCode.Type.SINGLE ->
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                Element.BlockCode.Type.MIDDLE ->
                    canvas.drawRect(rect, paint)
                Element.BlockCode.Type.START -> {
                    path.reset()
                    path.addRoundRect(
                        rect,
                        floatArrayOf(
                            cornerRadius, cornerRadius, // Top left radius in px
                            cornerRadius, cornerRadius, // Top right radius in px
                            0f, 0f, // Bottom right radius in px
                            0f, 0f // Bottom left radius in px
                        ),
                        Path.Direction.CW
                    )
                    canvas.drawPath(path, paint)
                }
                Element.BlockCode.Type.END -> {
                    path.reset()
                    path.addRoundRect(
                        rect,
                        floatArrayOf(
                            0f, 0f, // Top left radius in px
                            0f, 0f, // Top right radius in px
                            cornerRadius, cornerRadius, // Bottom right radius in px
                            cornerRadius, cornerRadius // Bottom left radius in px
                        ),
                        Path.Direction.CW
                    )
                    canvas.drawPath(path, paint)
                }
            }
        }

        paint.forText {
            canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
        }
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        if (fm != null) {
            fm.ascent =
                (paint.ascent() -
                        if (type == Element.BlockCode.Type.SINGLE || type == Element.BlockCode.Type.START)
                            2 * padding
                        else
                            0f
                        ).toInt()
            fm.descent = (paint.descent() +
                    if (type == Element.BlockCode.Type.SINGLE || type == Element.BlockCode.Type.END)
                        2 * padding
                    else
                        0f
                    ).toInt()
            fm.top = fm.ascent
            fm.bottom = fm.descent
        }
        return 0
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

//    private inline fun Paint.forLine(block: () -> Unit) {
//        val oldColor = color
//        val oldStyle = style
//        val oldWidth = strokeWidth
//        val oldPathEffect = pathEffect
//
////        pathEffect = dashs
//        color = textColor
//        style = Paint.Style.STROKE
//        strokeWidth = 0f
//
//        block.invoke()
//
//        pathEffect = oldPathEffect
//        strokeWidth = oldWidth
//        color = oldColor
//        style = oldStyle
//    }


}
