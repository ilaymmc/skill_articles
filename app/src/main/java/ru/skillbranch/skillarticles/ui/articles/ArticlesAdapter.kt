package ru.skillbranch.skillarticles.ui.articles

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_article.view.*
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView

class ArticlesAdapter(
    private val clickListener: (ArticleItemData) -> Unit,
    private val bookmarkToggleListener: (String, Boolean) -> Unit
) : PagedListAdapter<ArticleItemData, ArticleVH>(
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
        holder.bind(item, listener = clickListener, toggle = { checked ->
            item?.let { bookmarkToggleListener(it.id, checked) }
        } )
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
        item: ArticleItemData?,
        listener: (ArticleItemData) -> Unit,
        toggle: (Boolean) -> Unit
    ) {

        // item can be null if we use placeholder
        item?.let {
            containerView.bind(item, toggle)
            containerView.setOnClickListener { listener(item) }
            containerView.iv_bookmark
        }
    }
}