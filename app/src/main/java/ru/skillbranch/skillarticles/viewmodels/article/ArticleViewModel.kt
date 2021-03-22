package ru.skillbranch.skillarticles.viewmodels.article

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.ArticleData
import ru.skillbranch.skillarticles.data.local.entities.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.models.CommentItemData
import ru.skillbranch.skillarticles.data.repositories.*
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.shortFormat
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticleViewModel(handle: SavedStateHandle, private val articleId: String):
    BaseViewModel<ArticleState>(handle, ArticleState()), IArticleViewModel {

    private val repository = ArticleRepository
    private val rootRepository = RootRepository
    private var clearContent: String? = null

    private val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(5)
            .build()
    }

    private val listData: LiveData<PagedList<CommentItemData>> = Transformations.switchMap(repository.findArticleCommentCount(articleId)) {
        buildPagedList(repository.loadAllComments(articleId, it))
    }


    init {
        subscribeOnDataSource(repository.findArticle(articleId)) { article, state ->
            Log.d("ArticleViewModel", "getArticleData")
            if (article.content == null)
                fetchContent()
            state.copy(
                shareLink = article.shareLink,
                title = article.title,
                category = article.category.title,
                categoryIcon = article.category.icon,
                date = article.date.shortFormat(),
                author = article.author,
                isBookmark = article.isBookmark,
                isLike = article.isLike,
                content = article.content ?: emptyList(),
                isLoadingContent = article.content == null
            )
        }

        subscribeOnDataSource(repository.getAppSettings()) { settings, state ->
            Log.d("ArticleViewModel", "getAppSettings")
            state.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText
            )
        }

        subscribeOnDataSource(rootRepository.isAuth()) { isAuth, state ->
            state.copy(isAuth = isAuth)
        }

    }

    private fun fetchContent() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchArticleContent(articleId)
        }
    }

    override fun handleUpText() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isBigText = true))
    }

    override fun handleDownText() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isBigText = false))
    }

    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleLike() {
        val isLiked = currentState.isLike
        val msg = if (currentState.isLike) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage (
                "Don`t like it anymore",
                "No, still like it"
            ) {
                handleLike()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleLike(articleId)
            if (isLiked)
                repository.decrementLike(articleId)
            else
                repository.incrementLike(articleId)
            withContext(Dispatchers.Main) {
                notify(msg)
            }
        }
    }

    override fun handleBookmark() {
        val msg = if (!currentState.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(articleId)
            withContext(Dispatchers.Main) {
                notify(Notify.ErrorMessage(msg, "OK", null))
            }
        }
    }

    override fun handleShare() {
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    override fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    fun handleSearch(query: String?) {
        query ?: return
        if (currentState.isSearch && currentState.searchQuery != query) {
            if (clearContent == null && currentState.content.isNotEmpty())
                clearContent = currentState.content.clearContent()
            val result = clearContent.indexesOf(query)
                .map { it to it + query.length }

            val newPosition =
                if (currentState.searchPosition >= result.size) 0 else currentState.searchPosition

            updateState {
//                it.copy(searchQuery = query, searchResults = result, searchPosition = newPosition)
                it.copy(searchQuery = query, searchResults = result, searchPosition = 0)
            }
        }
    }

    fun handleSearchModel(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0) }
    }

    fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }

    fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }

    fun handleCopyCode() {
        notify(Notify.TextMessage("Code copy to clipboard"))

    }

    fun handleSendComment(comment: String) {
        if (!currentState.isAuth) {
            updateState { it.copy(commentText = comment) }
            navigate(NavigationCommand.StartLogin())
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                repository.sendMessage(articleId, comment, currentState.answerToSlug)
                withContext(Dispatchers.Main) {
                    updateState { it.copy(answerTo = null, answerToSlug = null) }
                }
            }
        }
    }

    fun observeList(
        owner: LifecycleOwner,
        onChanged: (list: PagedList<CommentItemData>) -> Unit
    ) {
        listData.observe(owner, Observer { onChanged(it) })
    }

    private fun buildPagedList(
        dataFactory: CommentsDataFactory
    ): LiveData<PagedList<CommentItemData>> =
        LivePagedListBuilder<String, CommentItemData> (
                    dataFactory,
                    listConfig)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()

    fun handleCommentFocus(hasFocus: Boolean) {
        updateState { it.copy(showBottomBar = !hasFocus) }
    }

    fun handleClearComment() {
        updateState { it.copy(answerTo = null, answerToSlug = null) }
    }

    fun handleReplyTo(slug: String, name: String) {
        updateState { it.copy(answerToSlug = slug, answerTo = "Reply to $name") }
    }

}

data class ArticleState(
    val isAuth: Boolean = false,
    val isLoadingContent: Boolean = true,
    val isLoadingReview: Boolean = true,
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isShowMenu: Boolean = false,
    val isBigText: Boolean = false,
    val isDarkMode: Boolean = false,
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val searchResults: List<Pair<Int, Int>> = emptyList(),
    val searchPosition: Int = 0,
    val shareLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val author: Any? = null,
    val poster: String? = null,
    internal val content: List<MarkdownElement> = emptyList(),
    val commentsCount: Int = 0,
    val answerTo: String? = null,
    val answerToSlug : String? = null,
    val showBottomBar: Boolean = true,
    val commentText : String? = null,
    val tags: List<String> = emptyList(),
    val source: String? = null
) : IViewModelState {
    override fun save(outState: SavedStateHandle) {
        outState.set("answerTo", answerTo)
        outState.set("answerToSlug", answerToSlug)

        outState.set("isSearch", isSearch)
        outState.set("searchPosition", searchPosition)
        outState.set("searchQuery", searchQuery)
        outState.set("searchResult", searchResults)
    }

    override fun restore(savedState: SavedStateHandle): IViewModelState {
        return copy(
            isSearch = savedState["isSearch"] ?: false,
            searchQuery = savedState["searchQuery"],
            searchResults = savedState["searchResult"] ?: emptyList(),
            searchPosition = savedState["searchPosition"] ?: 0,

            answerTo = savedState["answerTo"],
            answerToSlug = savedState["answerToSlug"]
        )
    }
}
