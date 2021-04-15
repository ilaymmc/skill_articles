package ru.skillbranch.skillarticles.ui.articles

import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.*
import android.widget.AutoCompleteTextView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.ui.base.*
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesState
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Loading
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class ArticlesFragment : BaseFragment<ArticlesViewModel>() {
    private lateinit var suggestionsAdapter: SimpleCursorAdapter
    override val viewModel: ArticlesViewModel by activityViewModels()
    override val layout = R.layout.fragment_articles
    override val binding: ArticlesBinding by lazy {
        ArticlesBinding()
    }
    private val args: ArticlesFragmentArgs by navArgs()

    override val prepareToolbar: (ToolbarBuilder.() -> Unit) = {
        addMenuItem(
            MenuItemHolder(
                "Search",
                R.id.action_search,
                R.drawable.ic_search_black_24dp,
                R.layout.search_view_layout
            )
        )
        addMenuItem(
            MenuItemHolder(
                "Filter",
                R.id.action_filter,
                R.drawable.ic_filter_list_black_24dp,
                null
            ) { _ ->
                val action = ArticlesFragmentDirections.choseCategory(
                    binding.selectedCategories.toTypedArray(),
                    binding.categories.toTypedArray()
                )
                viewModel.navigate(NavigationCommand.To(action.actionId, action.arguments))
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.handleSearchMode(false)
    }

    override fun renderLoading(loadingState: Loading) {
        when (loadingState) {
            Loading.SHOW_LOADING -> if (!refresh.isRefreshing) root.progress.isVisible = true
            Loading.SHOW_BLOCKING_LOADING -> root.progress.isVisible = false
            Loading.HIDE_LOADING -> {
                root.progress.isVisible = false
                if (refresh.isRefreshing) refresh.isRefreshing = false
            }
        }
    }

    private var articlesAdapter = ArticlesAdapter (
        clickListener = { item ->
            val direction = ArticlesFragmentDirections.actionNavArticlesToPageArticle(
                item.id,
                item.author,
                item.authorAvatar ?: "",
                item.category,
                item.categoryIcon,
                item.date,
                item.poster,
                item.title)

            viewModel.navigate(NavigationCommand.To(
                direction.actionId,
                direction.arguments
            ))
        },
        bookmarkToggleListener = { id, isChecked ->
            viewModel.handleToggleBookmark(id)
        }
    )

    override fun setupViews() {
        with(rv_articles) {
            layoutManager = LinearLayoutManager(context)
            adapter = articlesAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        viewModel.observeList(
            owner = viewLifecycleOwner,
            isBookmark = args.isBookmarks
        ) {
            articlesAdapter.submitList(it)
        }

        viewModel.observeTags(viewLifecycleOwner) {
            binding.tags = it
        }

        viewModel.observeCategories(viewLifecycleOwner) {
            binding.categories = it
        }

        refresh.setOnRefreshListener {
            viewModel.refresh()
        }
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        suggestionsAdapter = SimpleCursorAdapter(
            context,
            android.R.layout.simple_list_item_1,
            null,
            arrayOf("tag"),
            intArrayOf(android.R.id.text1),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        suggestionsAdapter.setFilterQueryProvider { constraint ->
            populateAdapter(constraint)
        }
    }

    private fun populateAdapter(constraint: CharSequence?): Cursor {
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, "tag"))
        constraint ?: return cursor

        val currentCursor = suggestionsAdapter.cursor
        currentCursor.moveToFirst()

        for (i in 0 until currentCursor.count) {
            val tagValue = currentCursor.getString(1)
            if (tagValue.contains(constraint, true)) {
                cursor.addRow(arrayOf<Any>(i, tagValue))
                currentCursor.moveToNext()
            }
        }
        return cursor
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        if (binding.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(binding.searchQuery, false)
            if (binding.isFocusedSearch) {
                searchView.requestFocus()
            } else {
                searchView.clearFocus()
            }
        } else {
            menuItem.collapseActionView()
        }

        searchView.findViewById<AutoCompleteTextView>(R.id.search_src_text)
            .threshold = 1

        searchView.suggestionsAdapter = suggestionsAdapter
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean = false

            override fun onSuggestionClick(position: Int): Boolean {
                suggestionsAdapter.cursor.moveToPosition(position)
                val tag = suggestionsAdapter.cursor.getString(1)
                searchView.setQuery(tag, true)
                viewModel.handleSuggestion(tag)
                return false
            }
        })


        menuItem.setOnActionExpandListener( object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })

        searchView.setOnCloseListener {
            viewModel.handleSearchMode(false)
            false
        }
    }

    inner class ArticlesBinding: Binding() {
        var categories: List<CategoryData> = emptyList()
        var selectedCategories: List<String> by RenderProp(emptyList()) {
            //TODO selected color on icon
        }
        var isFocusedSearch : Boolean = false
        var searchQuery: String? = null
        var isSearch: Boolean by RenderProp(false) {
        }
        var isLoading: Boolean by RenderProp(true) {
            //TODO show shimmer in rv_list
        }
        var isHastagSearch: Boolean by RenderProp(false)
        var tags: List<String> by RenderProp(emptyList())

        override fun bind(data: IViewModelState) {
            data as ArticlesState
            isSearch = data.isSearch
            isLoading = data.isLoading
            searchQuery = data.searchQuery
            isHastagSearch = data.isHashTagSearch
            selectedCategories = data.selectedCategories
        }
        // TODO save UI 47:45 ?
        override fun saveUi(outState: Bundle) {
            outState.putString(::searchQuery.name, searchQuery)
            outState.putBoolean(::isFocusedSearch.name, search_view?.hasFocus() ?: false)
            outState.putBoolean(::isLoading.name, isLoading)
        }
        override fun restoreUi(savedState: Bundle?) {
            searchQuery = savedState?.getString(::searchQuery.name) ?: ""
            isFocusedSearch = savedState?.getBoolean(::isFocusedSearch.name) ?: false
            isLoading = savedState?.getBoolean(::isLoading.name) ?: false
        }

        override val afterInflate: (() -> Unit)? = {
            dependsOn<Boolean, List<String>>(::isHastagSearch, ::tags) { ihs, tags ->
                val cursor = MatrixCursor(
                    arrayOf(
                        BaseColumns._ID,
                        "tag"
                    )
                )

                if (ihs && tags.isNotEmpty()) {
                    for ((counter, tag) in tags.withIndex()) {
                        cursor.addRow(arrayOf(counter, tag))
                    }
                }

                suggestionsAdapter.changeCursor(cursor)
            }
        }
    }
}