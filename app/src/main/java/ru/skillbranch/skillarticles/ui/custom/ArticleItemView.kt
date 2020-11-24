package ru.skillbranch.skillarticles.ui.custom

import android.R.attr.factor
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.item_article.view.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.ArticleItemData
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.setPadding


//Реализуй CustomViewGroup ArticleItemView содержащую метод bind(data : ArticleItemData) для связывания
//данных и представлений в ArticleItemView. Разметка ArticleItemView должна соответствовать item_article.xml
//и иметь первичный конструктор ArticleItemView(context:Context). Расположить ArticleItemView в
//package ru.skillbranch.skillarticles.ui.custom
//ВАЖНО: должны быть указаны id для сдедующих View:
//R.id.tv_title
//R.id.iv_poster
//R.id.tv_description
//R.id.tv_read_duration
//R.id.tv_author

class ArticleItemView constructor(
    context: Context
) : ConstraintLayout(context, null, 0) {

    @ColorInt
    private val colorPrimary: Int = context.attrValue(R.attr.colorPrimary)
    @ColorInt
    private val colorSurface: Int = context.attrValue(R.attr.colorSurface)
    @ColorInt
    private val colorOnSurface: Int  = context.attrValue(R.attr.colorOnSurface)
    @ColorInt
    private val colorOnBackground: Int  = context.attrValue(R.attr.colorOnBackground)

    private lateinit var tvTitle: TextView
    init {
        inflate(context, R.layout.item_article, this)
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(context.dpToIntPx(16))
//        tvTitle = TextView(context).apply {
//            text = "Test"
//            setTextColor(colorPrimary)
//            setBackgroundColor(ColorUtils.setAlphaComponent(colorSurface, 160))
//            gravity = Gravity.CENTER
//            textSize = 12f
////            setPadding(titleTopMargin)
//            isVisible = true
//        }
//        addView(tvTitle)
    }

//    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
//    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
////        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
////        setMeasuredDimension(parentWidth, measuredHeight)
//
////        val width = View.getDefaultSize(suggested, widthMeasureSpec)
////        var usedHeight = 0
////        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
////        val msw = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
////
////        tv_date.measure(msw, heightMeasureSpec)
////        usedHeight += tv_date.measuredHeight
////
////        tv_title.measure(msw, heightMeasureSpec)
////        usedHeight += tv_title.measuredHeight
//////        tvTitle.measure(msw, heightMeasureSpec)
//////        usedHeight += tvTitle.measuredHeight
////
////        tv_description.measure(msw, heightMeasureSpec)
////        usedHeight += tv_description.measuredHeight
////
////        tv_likes_count.measure(msw, heightMeasureSpec)
////        usedHeight += tv_likes_count.measuredHeight
////
//////        iv_image.measure(msw, heightMeasureSpec)
//////        tv_title.measure(msw, heightMeasureSpec)
////////        measureChild(tv_title, widthMeasureSpec, heightMeasureSpec)
//////        if (tv_alt != null)
//////            tv_alt?.measure(msw, heightMeasureSpec)
//////
//////        usedHeight += iv_image.measuredHeight
//////        usedHeight += titleTopMargin
//////        linePositionY = usedHeight + tv_title.measuredHeight / 2f
//////        usedHeight += tv_title.measuredHeight
////
////        setMeasuredDimension(width, usedHeight)
//    }
//
//    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
//    public override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        super.onLayout(changed, l, t, r, b)
////        var usedHeight = 0
////        val bodyWidth = r - l - paddingLeft - paddingRight
////        val left = paddingLeft
////        val right = left + bodyWidth
////
////        tv_date.layout(
////            left,
////            usedHeight,
////            left + tv_date.measuredWidth,
////            usedHeight + tv_date.measuredHeight
////        )
////
////        usedHeight += tv_date.measuredHeight
////        usedHeight += context.dpToIntPx(8)
////
//////        tvTitle.layout(
//////            left,
//////            usedHeight,
//////            right,
//////            usedHeight + tvTitle.measuredHeight
//////        )
//////        usedHeight += tvTitle.measuredHeight
////        tv_title.layout(
////            left,
////            usedHeight,
////            right,
////            usedHeight + tv_title.measuredHeight
////        )
////        usedHeight += tv_title.measuredHeight
////
////        tv_description.layout(
////            left,
////            usedHeight,
////            right,
////            usedHeight + tv_description.measuredHeight
////        )
////        usedHeight += tv_title.measuredHeight
////
////        iv_likes?.layout(
////            left,
////            usedHeight,
////            left + iv_likes.measuredWidth,
////            usedHeight + iv_likes.measuredHeight
////        )
//    }

    fun bind(data: ArticleItemData) {

        val posterSize = context.dpToIntPx(64)
        val categorySize = context.dpToIntPx(40)
        val cornerRadius = context.dpToIntPx(8)

        com.bumptech.glide.Glide.with(context)
            .load(data.poster)
            .transform(
                com.bumptech.glide.load.resource.bitmap.CenterCrop(),
                com.bumptech.glide.load.resource.bitmap.RoundedCorners(cornerRadius)
            )
            .override(posterSize)
            .into(iv_poster)

        com.bumptech.glide.Glide.with(context)
            .load(data.categoryIcon)
            .transform(
                com.bumptech.glide.load.resource.bitmap.CenterCrop(),
                com.bumptech.glide.load.resource.bitmap.RoundedCorners(cornerRadius)
            )
            .override(categorySize)
            .into(iv_category)

        tv_date.text = data.date.format()
        tv_author.text = data.author
        tv_title.text = data.title
//        tvTitle.text = data.title
        tv_description.text = data.description
        tv_likes_count.text = "${data.likeCount}"
        tv_comments_count.text = "${data.commentCount}"
        tv_read_duration.text = "${data.readDuration} mins read"

    }
}