package ru.skillbranch.skillarticles.viewmodels.articles

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.repositories.ArticlesDataFactory
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.data.repositories.ArticlesStrategy
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticlesViewModel(handle: SavedStateHandle) : BaseViewModel<ArticlesState>(handle, ArticlesState()) {
    private val repository = ArticlesRepository
    private val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(10)
//            .setPrefetchDistance(0)
            .setInitialLoadSizeHint(50)
            .build()
    }
    private val listData = Transformations.switchMap(state) {
        when {
            it.isSearch && !it.searchQuery.isNullOrBlank() -> buildPageList(repository.searchArticles(it.searchQuery))
            else -> buildPageList(repository.allArticles())
        }
    }


    fun observeList(
        owner: LifecycleOwner,
        onChange: (list: PagedList<ArticleItemData>) -> Unit
    ) {
        listData.observe(owner, Observer {
            onChange(it)
        })

    }

    private fun buildPageList(
        dataFactory: ArticlesDataFactory
    ): LiveData<PagedList<ArticleItemData>> {
        val builder = LivePagedListBuilder<Int, ArticleItemData>(
            dataFactory,
            listConfig
        )

        if (dataFactory.strategy is ArticlesStrategy.AllArticles) {
            builder.setBoundaryCallback(ArticlesBoundaryCallback(
                ::zeroLoadingHandler,
                ::itemAtEndHandler
            ))
        }

        return builder
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    private var isLoading = false

    private fun itemAtEndHandler(articleItemData: ArticleItemData) {
        Log.e("ArticlesViewModel", "itemAtEndHandler(${articleItemData.id})")
        if (isLoading)
            return
//        notify(Notify.TextMessage("End reached"))
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            val items = repository.loadArticlesFromNetwork(
                start = articleItemData.id.toInt().inc(),
                size = listConfig.pageSize)
            if (items.isNotEmpty()) {
                repository.insertArticlesToDd(items)
                Log.e("ArticlesViewModel", "invalidate new ${items.size}")
                listData.value?.dataSource?.invalidate()
            }
            isLoading = false

//            withContext(Dispatchers.Main) {
//                notify(Notify.TextMessage("Load from network articles from ${items.firstOrNull()?.id} " +
//                        "to ${items.lastOrNull()?.id}"))
//            }
        }
    }

    private fun zeroLoadingHandler() {
        Log.e("ArticlesViewModel", "zeroLoadingHandler()")
        notify(Notify.TextMessage("Storage is empty"))
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(0, listConfig.initialLoadSizeHint)
            if (items.isNotEmpty()) {
                repository.insertArticlesToDd(items)
                Log.e("ArticlesViewModel", "invalidate new ${items.size}")
                listData.value?.dataSource?.invalidate()
            }
            withContext(Dispatchers.Main) {
                notify(Notify.TextMessage("Load from network articles from ${items.firstOrNull()?.id} " +
                        "to ${items.lastOrNull()?.id}"))
            }
        }
    }

    fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy( isSearch = isSearch ) }
    }

    fun handleSearch(newText: String?) {
        newText ?: return
        updateState { it.copy( searchQuery = newText ) }
    }
}

data class ArticlesState (
    val isSearch : Boolean = false,
    val searchQuery : String? = null,
    val isLoading: Boolean = true
) : IViewModelState

class ArticlesBoundaryCallback(
    private val zeroLoadingHandler: () -> Unit,
    private val itemAtEndHandler: (itemAtEnd: ArticleItemData) -> Unit
): PagedList.BoundaryCallback<ArticleItemData>() {
    override fun onZeroItemsLoaded() {
        zeroLoadingHandler()
    }

    override fun onItemAtEndLoaded(itemAtEnd: ArticleItemData) {
        itemAtEndHandler(itemAtEnd)
    }
}
