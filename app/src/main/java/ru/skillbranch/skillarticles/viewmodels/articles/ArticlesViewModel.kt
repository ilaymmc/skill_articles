package ru.skillbranch.skillarticles.viewmodels.articles

import android.util.Log
//import androidx.core.content.contentValuesOf
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.data.repositories.ArticleFilter
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticlesViewModel(handle: SavedStateHandle) : BaseViewModel<ArticlesState>(handle, ArticlesState()) {
    private val repository = ArticlesRepository
    private var isLoadingInitial = false
    private var isLoadingAfter = false
    private val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(30)
            .setInitialLoadSizeHint(50)
            .build()
    }

//    private val allArticlesList by lazy {
//        buildPageList(repository.allArticles())
//    }

    private val listData =
        Transformations.switchMap(state) {
            val filter = it.toArticleFilter()
            return@switchMap buildPageList(repository.rawQueryArticles(filter))
        }


    fun observeList(
        owner: LifecycleOwner,
        isBookmark: Boolean = false,
        onChange: (list: PagedList<ArticleItem>) -> Unit
    ) {
        updateState { it.copy(isBookmark = isBookmark) }
        listData.observe(owner, Observer {
            onChange(it)
        })
    }

    fun observeTags(owner: LifecycleOwner, onChange: (list: List<String>) -> Unit) {
        repository.findTags().observe(owner, Observer(onChange))
    }

    fun observeCategories(owner: LifecycleOwner, onChange: (list: List<CategoryData>) -> Unit) {
        repository.findCategoriesData().observe(owner, Observer(onChange))
    }

    private fun buildPageList(
        dataFactory: DataSource.Factory<Int, ArticleItem>
    ): LiveData<PagedList<ArticleItem>> {
        val builder = LivePagedListBuilder<Int, ArticleItem>(
            dataFactory,
            listConfig
        )

        if (isEmptyFilter()) {
            builder.setBoundaryCallback(ArticlesBoundaryCallback(
                ::zeroLoadingHandler,
                ::itemAtEndHandler
            ))
        }

        return builder
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    private fun isEmptyFilter(): Boolean =
        currentState.run { searchQuery.isNullOrEmpty() && !isBookmark &&
                selectedCategories.isEmpty() && !isHashTagSearch}

    private fun itemAtEndHandler(lastLoadArticle: ArticleItem) {
        Log.e("ArticlesViewModel", "itemAtEndHandler(${lastLoadArticle.id})")
        if (isLoadingAfter)
            return
        isLoadingAfter = true
        launchSafety( null, { isLoadingAfter = false }) {
            val count = repository.loadArticlesFromNetwork(
                start = lastLoadArticle.id,
                size = listConfig.pageSize
            )
            notify(Notify.TextMessage("Load $count items paged"))
        }
    }

    private fun zeroLoadingHandler() {
        Log.e("ArticlesViewModel", "zeroLoadingHandler()")
        if (isLoadingInitial)
            return
        isLoadingInitial = true
        notify(Notify.TextMessage("Storage is empty"))

        launchSafety( null, { isLoadingInitial = false }) {
            val count = repository.loadArticlesFromNetwork(
                start = null,
                size = listConfig.initialLoadSizeHint
            )
            notify(Notify.TextMessage("Load $count items initial"))
        }
    }

    fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy( isSearch = isSearch ) }
    }

    fun handleSearch(query: String?) {
        query ?: return
        updateState { it.copy( searchQuery = query, isHashTagSearch = query.startsWith("#", true)) }
    }

    fun handleToggleBookmark(articleId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(articleId)
        }
    }

    fun handleSuggestion(tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementTagUseCount(tag)
        }
    }

    fun applyCategories(selectedCategories: List<String>) {
        updateState { it.copy(selectedCategories = selectedCategories) }
    }

    fun refresh() {
        isLoadingInitial = true
        launchSafety (null, { isLoadingInitial = false } ) {
            val lastArticleId = repository.findLastArticleId()
            val count = repository.loadArticlesFromNetwork(
                start = lastArticleId,
                size = if (lastArticleId == null) listConfig.initialLoadSizeHint else -listConfig.pageSize
            )
            notify(Notify.TextMessage("Load $count new articles"))
        }
    }

}

private fun ArticlesState.toArticleFilter() = ArticleFilter(
    search = searchQuery,
    isBookmark = isBookmark,
    categories = selectedCategories,
    isHashtag = isHashTagSearch
)

data class ArticlesState (
    val isSearch : Boolean = false,
    val isBookmark: Boolean = false,
    val searchQuery : String? = null,
    val isLoading: Boolean = true,
    val selectedCategories: List<String> = emptyList(),
    val isHashTagSearch: Boolean = false
) : IViewModelState

class ArticlesBoundaryCallback(
    private val zeroLoadingHandler: () -> Unit,
    private val itemAtEndHandler: (itemAtEnd: ArticleItem) -> Unit
): PagedList.BoundaryCallback<ArticleItem>() {
    override fun onZeroItemsLoaded() {
        zeroLoadingHandler()
    }

    override fun onItemAtEndLoaded(itemAtEnd: ArticleItem) {
        itemAtEndHandler(itemAtEnd)
    }
}
