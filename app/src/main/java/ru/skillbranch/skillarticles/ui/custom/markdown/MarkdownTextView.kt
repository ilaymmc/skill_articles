package ru.skillbranch.skillarticles.ui.custom.markdown

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.text.Spanned
import android.util.AttributeSet
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation

@SuppressLint("ViewConstructor")
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
class MarkdownTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val searchBgHelper = SearchBgHelper(context) {

    }

    override fun onDraw(canvas: Canvas) {
        if (text is Spanned && layout != null) {
            canvas.withTranslation (totalPaddingLeft.toFloat(), totalPaddingRight.toFloat()) {
                searchBgHelper.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }
}
//
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