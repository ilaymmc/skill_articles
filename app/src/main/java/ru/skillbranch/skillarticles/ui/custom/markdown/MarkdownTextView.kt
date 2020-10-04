package ru.skillbranch.skillarticles.ui.custom.markdown

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue

@SuppressLint("ViewConstructor")
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
class MarkdownTextView @JvmOverloads constructor(
    context: Context,
    fontSize: Float
) : AppCompatTextView(context, null, 0), IMarkdownView {

    override fun onDraw(canvas: Canvas) {
        if (text is Spanned && layout != null) {
            canvas.withTranslation (totalPaddingLeft.toFloat(), totalPaddingRight.toFloat()) {
                searchBgHelper.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }

    override var fontSize: Float = fontSize
        set(value) {
            textSize = value
            field = value
        }

    override val spannableContent: Spannable
        get() = text as Spannable

    val color = context.attrValue(R.attr.colorOnBackground)

    private val searchBgHelper = SearchBgHelper(context) {

    }

    init {
//        setBackgroundColor(Color.GREEN)
        setTextColor(color)
        textSize = fontSize
        movementMethod = LinkMovementMethod.getInstance()
    }
}

//class MarkdownTextView constructor(
//    context: Context,
//    fontSize: Float,
//    mockHelper: SearchBgHelper? = null //for mock
//) : TextView(context, null, 0), IMarkdownView {
//
//    constructor(context: Context, fontSize: Float) : this(context, fontSize, null)
//
//    override var fontSize: Float
//
//    override val spannableContent: Spannable
//
//    private val color  //colorOnBackground
//    private val focusRect = Rect()
//
//    private val searchBgHelper = SearchBgHelper(context) { top, bottom ->
//        //TODO implement me
//    }
//
//
//    override fun onDraw(canvas: Canvas) {
//        //TODO implement me
//        super.onDraw(canvas)
//    }
//}