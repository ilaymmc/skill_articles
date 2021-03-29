package ru.skillbranch.skillarticles.ui.dialogs

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_choose_dialog.view.*
import kotlinx.android.synthetic.main.item_category.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel

/*
class ChoseCategoryDialog : DialogFragment() {
    private val viewModel: ArticlesViewModel by activityViewModels()
    private val selectedCategories = mutableListOf<String>()
    private val args: ChoseCategoryDialogArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // TODO save checked state and implements custom items
        val categories = args.categories.toList().map { "${it.title} (${it.articlesCount})" }.toTypedArray()
        val checked = BooleanArray(args.categories.size) {
            args.selectedCategories.contains(args.categories[it].categoryId)
        }
        val adb = AlertDialog.Builder(requireContext())
            .setTitle("Choose category")
            .setPositiveButton("Apply") { _, _ ->
                viewModel.applyCategories(selectedCategories)
            }
            .setNegativeButton("Reset") { _, _ ->
                viewModel.applyCategories(emptyList<String>())
            }
            .setMultiChoiceItems(categories, checked) { dialog, which, isChecked ->
                if (isChecked) {
                    selectedCategories.add(args.categories[which].categoryId)
                } else {
                    selectedCategories.remove(args.categories[which].categoryId)
                }
            }
        return adb.create()
    }
}
*/




//    Реализуй ChooseCategoryDialog отображающий список категорий (android:id="@+id/categories_list"),
//    каждый элемент списка должен содержать CheckBox (android:id="@+id/ch_select"), иконку категории (android:id="@+id/iv_icon"),
//    название категории (android:id="@+id/tv_category"), количество статей в категории (android:id="@+id/tv_count")
//    При перевороте экрана необходимо чтобы сохранялись состояния CheckBox выбранных катеогрий.
//    Диалоговое окно должно содержать два действия Reset (сбросить все выбранные категории),
//    Apply (применить выбранные категории к фильтру). Отображать диалоговое окно необходимо при клике на
//    Toolbar MenuItem с идентификатором (android:id="@+id/action_filter")

class ChooseCategoryDialog : DialogFragment() {
    private val viewModel: ArticlesViewModel by activityViewModels()
    private val selectedCategories = mutableListOf<String>()
    private val args: ChooseCategoryDialogArgs by navArgs()

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray("selectedCategories", selectedCategories.toTypedArray())
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val savedSelection = savedInstanceState?.getStringArray("selectedCategories")
        (savedSelection ?: args.selectedCategories).map {
            selectedCategories.add(it)
        }
        val categories = args.categories
        val checked = BooleanArray(categories.size) {
            selectedCategories.contains(categories[it].categoryId)
        }
        val categoriesAdapter = CategoriesAdapter(checked) { which, isChecked, _ ->
            checked[which] = isChecked
            if (isChecked) {
                selectedCategories.add(categories[which].categoryId)
            } else {
                selectedCategories.remove(categories[which].categoryId)
            }
        } .apply {
            submitList(categories.toList())
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val child = inflater.inflate(R.layout.fragment_choose_dialog, null)
                ?.categories_list?.apply {
                    adapter = categoriesAdapter
                    layoutManager = LinearLayoutManager(context)
                }
            builder.setView(child)
                .setTitle("Choose category")
                .setPositiveButton("Apply") { _, _ ->
                    viewModel.applyCategories(selectedCategories)
                }
                .setNegativeButton("Reset") { _, _ ->
                    viewModel.applyCategories(emptyList<String>())
                }
            // Add action buttons
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class CategoriesAdapter(
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
        }
    }
}

class CategoryDiffCallback: DiffUtil.ItemCallback<CategoryData>(){
    override fun areItemsTheSame(oldItem: CategoryData, newItem: CategoryData): Boolean =
        oldItem.categoryId == newItem.categoryId
    override fun areContentsTheSame(oldItem: CategoryData, newItem: CategoryData): Boolean =
        oldItem == newItem

}
