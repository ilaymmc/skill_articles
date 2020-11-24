package ru.skillbranch.skillarticles.ui.articles

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import ru.skillbranch.skillarticles.data.ArticleItemData
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView

//import kotlinx.android.synthetic.main.item_article.*


class ArticlesAdapter(private val listener: (ArticleItemData) -> Unit) : ListAdapter<ArticleItemData, ArticleVH>(
    ArticleDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        val containerView = ArticleItemView(parent.context)
//        val lp = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
//        containerView.layoutParams = lp
//            LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleVH(containerView)
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        holder.bind(getItem(position), listener = listener)
    }
}

class ArticleDiffCallback: DiffUtil.ItemCallback<ArticleItemData>(){
    override fun areItemsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean =
        oldItem == newItem
}

class ArticleVH(override val containerView: ArticleItemView) : RecyclerView.ViewHolder(containerView), LayoutContainer{
    fun bind(
        item: ArticleItemData,
        listener: (ArticleItemData) -> Unit
    ) {

        containerView.bind(item)
        containerView.setOnClickListener { listener(item) }

//        val posterSize = containerView.context.dpToIntPx(64)
//        val categorySize = containerView.context.dpToIntPx(40)
//        val cornerRadius = containerView.context.dpToIntPx(8)
//
//        with(containerView) {
//            Glide.with(containerView)
//                .load(item.poster)
//                .transform(CenterCrop(), RoundedCorners(cornerRadius))
//                .override(posterSize)
//                .into(iv_poster)
//
//            Glide.with(containerView)
//                .load(item.categoryIcon)
//                .transform(CenterCrop(), RoundedCorners(cornerRadius))
//                .override(categorySize)
//                .into(iv_category)
//
//            tv_date.text = item.date.format()
//            tv_author.text = item.author
//            tv_title.text = item.title
//            tv_description.text = item.description
//            tv_likes_count.text = "${item.likeCount}"
//            tv_comments_count.text = "${item.commentCount}"
//            tv_read_duration.text = "${item.readDuration} mins read"
//
//            itemView.setOnClickListener { listener(item) }
//
//        }
    }
}