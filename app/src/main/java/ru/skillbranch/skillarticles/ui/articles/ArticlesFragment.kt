package ru.skillbranch.skillarticles.ui.articles

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.base.*
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesState
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class ArticlesFragment : BaseFragment<ArticlesViewModel>() {
    override val viewModel: ArticlesViewModel by viewModels()

    override val layout = R.layout.fragment_articles
    override val binding: ArticlesBinding by lazy {
        ArticlesBinding()
    }

    override val prepareToolbar: (ToolbarBuilder.() -> Unit) = {
        addMenuItem(
            MenuItemHolder(
                "Search",
                R.id.action_search,
                R.drawable.ic_search_black_24dp,
                R.layout.search_view_layout
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.handleSearchMode(false)
    }

    private var articlesAdapter = ArticlesAdapter (
        clickListener = { item ->
            val direction = ArticlesFragmentDirections.actionNavArticlesToPageArticle(
                item.id,
                item.author,
                item.authorAvatar,
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
            viewModel.handleToggleBookmark(id, !isChecked)
        }
    )

    override fun setupViews() {
        with(rv_articles) {
            layoutManager = LinearLayoutManager(context)
            adapter = articlesAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        viewModel.observeList(viewLifecycleOwner) {
            articlesAdapter.submitList(it)
        }
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        var isFocusedSearch : Boolean = false
        var searchQuery: String? = null
        var isSearch: Boolean by RenderProp(false) {
        }
        var isLoading: Boolean by RenderProp(true) {
            //TODO show shimmer in rv_list
        }

        override fun bind(data: IViewModelState) {
            data as ArticlesState
            isSearch = data.isSearch
            isLoading = data.isLoading
            searchQuery = data.searchQuery
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
    }
}