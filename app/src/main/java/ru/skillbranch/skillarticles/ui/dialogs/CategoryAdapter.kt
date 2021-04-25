package ru.skillbranch.skillarticles.ui.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_category.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.extensions.dpToIntPx

class CategoryAdapter(
    private val checked: BooleanArray,
    private val listener: (Int, Boolean, CategoryData) -> Unit
) : ListAdapter<CategoryData, CategoryVH>(CategoryDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val containerView = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryVH(containerView)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        holder.bind(getItem(position), checked[position]) { checked, category ->
            listener.invoke(position, checked, category)
        }
    }
}

class CategoryVH(override val containerView: View):
    RecyclerView.ViewHolder(containerView), LayoutContainer {

    private val cornerRadius = containerView.context.dpToIntPx(8)
    private val categorySize = containerView.context.dpToIntPx(40)

    fun bind(item: CategoryData?, isChecked: Boolean, listener: (Boolean, CategoryData) -> Unit) {
        if (item != null) {
            ch_select.setOnCheckedChangeListener(null)
            Glide.with(containerView)
                .load(item.icon)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .override(categorySize)
                .into(iv_icon)
            tv_category.text = item.title
            tv_count.text = "${item.articlesCount}"
            ch_select.isChecked = isChecked
            ch_select.setOnCheckedChangeListener { _, checked ->
                listener.invoke(checked, item)
            }
            itemView.setOnClickListener { ch_select.toggle() }
        }
    }
}

class CategoryDiffCallback: DiffUtil.ItemCallback<CategoryData>(){
    override fun areItemsTheSame(oldItem: CategoryData, newItem: CategoryData): Boolean =
        oldItem.categoryId == newItem.categoryId
    override fun areContentsTheSame(oldItem: CategoryData, newItem: CategoryData): Boolean =
        oldItem == newItem
}
