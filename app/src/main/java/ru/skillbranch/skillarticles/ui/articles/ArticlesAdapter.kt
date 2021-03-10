package ru.skillbranch.skillarticles.ui.articles

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_article.view.*
import ru.skillbranch.skillarticles.data.local.entitles.ArticleItem
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView

class ArticlesAdapter(
    private val clickListener: (ArticleItem) -> Unit,
    private val bookmarkToggleListener: (String, Boolean) -> Unit
) : PagedListAdapter<ArticleItem, ArticleVH>(
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
        val item = getItem(position)
        holder.bind(item, listener = clickListener, toggle = { id, checked ->
            item?.let { bookmarkToggleListener(id, checked) }
        } )
    }
}

class ArticleDiffCallback: DiffUtil.ItemCallback<ArticleItem>(){
    override fun areItemsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean =
        oldItem == newItem
}

class ArticleVH(override val containerView: ArticleItemView) : RecyclerView.ViewHolder(containerView), LayoutContainer{
    fun bind(
        item: ArticleItem?,
        listener: (ArticleItem) -> Unit,
        toggle: (String, Boolean) -> Unit
    ) {

        // item can be null if we use placeholder
        item?.let {
            containerView.bind(item, toggle)
            containerView.setOnClickListener { listener(item) }
            containerView.iv_bookmark
        }
    }
}