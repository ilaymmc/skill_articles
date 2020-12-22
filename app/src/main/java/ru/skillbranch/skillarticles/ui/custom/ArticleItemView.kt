package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.extensions.*
import kotlin.math.*

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

    @Px
    private val smallMargin: Int = context.dpToIntPx(8)
    @Px
    private val mediumMargin: Int = context.dpToIntPx(16)
    @Px
    private val posterBottomMargin: Int = context.dpToIntPx(20)
    @Px
    private val titlePosterMargin: Int = context.dpToIntPx(24)
    @Px
    private val titleTopBottomMargin: Int = smallMargin // 0
    @ColorInt
    private val colorGray: Int = context.getColor(R.color.color_gray)
    @ColorInt
    private val colorPrimary: Int = context.attrValue(R.attr.colorPrimary)

    private val posterSize = context.dpToIntPx(64)
    private val categorySize = context.dpToIntPx(40)
    private val iconSize = context.dpToIntPx(16)

    private val tv_date: TextView
    private val tv_author: TextView
    private val tv_title: TextView
    private val iv_poster: ImageView
    private val iv_category: ImageView

    private val tv_description: TextView
    private val tv_likes_count: TextView
    private val tv_comments_count: TextView
    private val tv_read_duration: TextView

    private val iv_likes: ImageView
    private val iv_comments: ImageView
    private val iv_bookmark: ImageView

    init {
//        inflate(context, R.layout.item_article, this)
        layoutParams = MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(mediumMargin)

        tv_date = TextView(context).apply {
            id = R.id.tv_date
            setTextColor(colorGray)
            textSize = 12f
        }.also { addView(it) }


        tv_author = TextView(context).apply {
            id = R.id.tv_author
            setTextColor(colorPrimary)
            textSize = 12f
        }.also { addView(it) }

        tv_title = TextView(context).apply {
            id = R.id.tv_title
            setTextColor(colorPrimary)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
        }.also { addView(it) }

        iv_poster = ImageView(context).apply {
            id = R.id.iv_poster
        }.also { addView(it, ViewGroup.LayoutParams(posterSize, posterSize)) }

        iv_category= ImageView(context).apply {
            id = R.id.iv_category
        }.also { addView(it, ViewGroup.LayoutParams(categorySize, categorySize)) }

        tv_description = TextView(context).apply {
            id = R.id.tv_description
            compoundDrawables.forEach { it?.setTint(colorGray) }
            textSize = 14f
        }.also { addView(it) }

        iv_likes = ImageView(context).apply {
            id = R.id.iv_likes
            setImageDrawable(
                context.getDrawable(R.drawable.ic_favorite_black_24dp)!!.apply { setTint(colorGray) }
            )
        }.also { addView(it, ViewGroup.LayoutParams(iconSize, iconSize)) }

        tv_likes_count = TextView(context).apply {
            id = R.id.tv_likes_count
            setTextColor(colorGray)
            textSize = 12f
        }.also { addView(it) }

        tv_comments_count = TextView(context).apply {
            id = R.id.tv_comments_count
            setTextColor(colorGray)
            textSize = 12f
        }.also { addView(it) }

        iv_comments = ImageView(context).apply {
            id = R.id.iv_comments
            setImageDrawable(
                context.getDrawable(R.drawable.ic_insert_comment_black_24dp)!!.apply { setTint(colorGray) }
            )
        }.also { addView(it, ViewGroup.LayoutParams(iconSize, iconSize)) }

        tv_read_duration = TextView(context).apply {
            id = R.id.tv_read_duration
            setTextColor(colorGray)
            textSize = 12f
        }.also { addView(it) }

        iv_bookmark = ImageView(context).apply {
            id = R.id.iv_bookmark
            setImageDrawable(
                context.getDrawable(R.drawable.bookmark_states)!!.apply { setTint(colorGray) }
            )
        }.also { addView(it, ViewGroup.LayoutParams(iconSize, iconSize)) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = mediumMargin
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val widthBody = width - paddingLeft - paddingRight
        val msw = MeasureSpec.makeMeasureSpec(widthBody, MeasureSpec.EXACTLY)
        val mswMax = MeasureSpec.makeMeasureSpec(widthBody, MeasureSpec.AT_MOST)

        tv_date.measure(mswMax, heightMeasureSpec)
        tv_author.measure(
            MeasureSpec.makeMeasureSpec(widthBody - tv_date.measuredWidth - mediumMargin, MeasureSpec.EXACTLY),
            heightMeasureSpec)
        usedHeight += max(tv_date.measuredHeight, tv_author.measuredHeight)

//        usedHeight += smallMargin

        with(iv_poster) {
            measure(
                MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY)
            )
        }

        with(iv_category) {
            measure(
                MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY)
            )
        }

        tv_title.measure(
            MeasureSpec.makeMeasureSpec(widthBody - iv_poster.measuredWidth - titlePosterMargin, MeasureSpec.EXACTLY),
            heightMeasureSpec)

        val iconsHeight = smallMargin + iv_poster.measuredHeight + posterBottomMargin

        usedHeight += max(titleTopBottomMargin + tv_title.measuredHeight + titleTopBottomMargin, iconsHeight + smallMargin)

        tv_description.measure(msw, heightMeasureSpec)
        usedHeight += tv_description.measuredHeight

        usedHeight += smallMargin

        tv_likes_count.measure(mswMax, heightMeasureSpec)
        tv_comments_count.measure(mswMax, heightMeasureSpec)
        tv_read_duration.measure(mswMax, heightMeasureSpec)
        with(iv_likes) {
            measure(
                MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY)
            )
        }
        with(iv_comments) {
            measure(
                MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY)
            )
        }
        with(iv_bookmark) {
            measure(
                MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY)
            )
        }
        usedHeight += max(tv_likes_count.measuredHeight, iv_likes.measuredHeight)

        usedHeight += mediumMargin
        setMeasuredDimension(width, usedHeight)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = paddingTop
        val bodyWidth = r - l - paddingLeft - paddingRight
        val left = paddingLeft
        val right = left + bodyWidth

        tv_date.layout(
            left,
            usedHeight,
            left + tv_date.measuredWidth,
            usedHeight + tv_date.measuredHeight
        )

        tv_author.layout(
            left + tv_date.measuredWidth + mediumMargin,
            usedHeight,
            right,
            usedHeight + tv_author.measuredHeight
        )

        usedHeight += max(tv_date.measuredHeight, tv_author.measuredHeight)
//        usedHeight += smallMargin

        val posterHeight = iv_poster.measuredHeight + posterBottomMargin

        val lineHeight = max(
            titleTopBottomMargin + tv_title.measuredHeight + titleTopBottomMargin,
            smallMargin + posterHeight + smallMargin
        )

        val poserTop =  usedHeight + smallMargin + (lineHeight - smallMargin * 2 - posterHeight) / 2

        iv_poster.layout(
            right - iv_poster.measuredWidth,
            poserTop,
            right,
            poserTop + iv_poster.measuredHeight
        )

        val catLeft = iv_poster.left - iv_category.measuredWidth / 2
        val catTop = iv_poster.bottom - iv_category.measuredHeight / 2
        iv_category.layout(
            catLeft,
            catTop,
            catLeft + iv_category.measuredWidth,
            catTop + iv_category.measuredHeight
        )

        val titleTop = usedHeight + max((lineHeight - tv_title.measuredHeight - titleTopBottomMargin * 2) / 2, 0)
        tv_title.layout(
            left,
            titleTop + titleTopBottomMargin,
            right - iv_poster.measuredWidth - titlePosterMargin,
            titleTop + titleTopBottomMargin + tv_title.measuredHeight
        )
        usedHeight = max(iv_category.bottom + smallMargin, tv_title.bottom + titleTopBottomMargin)
        tv_description.layout(
            left,
            usedHeight,
            right,
            usedHeight + tv_description.measuredHeight
        )
        usedHeight += tv_description.measuredHeight

        usedHeight += smallMargin
        var curLeft = left
        iv_likes.apply {
            layout(curLeft, usedHeight,
                curLeft + measuredWidth, usedHeight + measuredHeight)
        }
        curLeft += iv_likes.measuredWidth
        curLeft += smallMargin
        tv_likes_count.apply {
            layout(curLeft, usedHeight,
                curLeft + measuredWidth, usedHeight + measuredHeight)
        }
        curLeft += tv_likes_count.measuredWidth
        curLeft += mediumMargin
        iv_comments.apply {
            layout(curLeft, usedHeight,
                curLeft + measuredWidth, usedHeight + measuredHeight)
        }
        curLeft += iv_comments.measuredWidth
        curLeft += smallMargin
        tv_comments_count.apply {
            layout(curLeft, usedHeight,
                curLeft + measuredWidth, usedHeight + measuredHeight)
        }
        curLeft += tv_comments_count.measuredWidth
        curLeft += mediumMargin

        iv_bookmark.apply {
            layout(right - measuredWidth, usedHeight,
                right, usedHeight + measuredHeight)
        }
        val rightBorder = iv_bookmark.left - mediumMargin

        tv_read_duration.apply {
            layout(curLeft, usedHeight,
                rightBorder, usedHeight + measuredHeight)
        }
    }

    fun bind(data: ArticleItemData) {

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

        tv_description.text = data.description
        tv_likes_count.text = "${data.likeCount}"
        tv_comments_count.text = "${data.commentCount}"
        tv_read_duration.text = "${data.readDuration} mins read"

    }
}