package ru.skillbranch.skillarticles.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import java.util.*

class ArticleViewModel(private val articleId: String) : BaseViewModel<ArticleState>(ArticleState()) {

    private val repository = ArticleRepository
    init {
        subscribeOnDataSource(getArticleData()) { article, state ->
            article ?: return@subscribeOnDataSource null
            Log.d("ArticleViewModel", "getArticleData")
            state.copy(
                shareLink = article.shareLink,
                title = article.title,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format(),
                author = article.author,
                poster = article.poster
            )
        }

        subscribeOnDataSource(getArticleContent()) { content, state ->
            content ?: return@subscribeOnDataSource null
            Log.d("ArticleViewModel", "getArticleContent")
            state.copy(
                isLoadingContent = false,
                content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()) { info, state ->
            info ?: return@subscribeOnDataSource null
            Log.d("ArticleViewModel", "getArticlePersonalInfo")
            state.copy(
                isBookmark = info.isBookmark,
                isLike = info.isLike
            )
        }

        subscribeOnDataSource(repository.getAppSettings()) { settings, state ->
            Log.d("ArticleViewModel", "getAppSettings")
            state.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText
            )
        }

    }

    private fun getArticleContent() : LiveData<List<Any>?> {
        return repository.loadArticleContent(articleId)
    }

    private fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }
    private fun getArticlePersonalInfo() : LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

    fun handleUpText() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isBigText = true))
    }

    fun handleDownText() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isBigText = false))
    }

    fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    fun handleLike() {
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersinalInfo(info.copy(isLike = !info.isLike))
        }
        toggleLike()

        val msg = if (currentState.isLike) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage (
                "Don`t like it anymore",
                "No, still like it",
                toggleLike
            )
        }

        notify(msg)
    }

    fun handleBookmark() {
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersinalInfo(info.copy(
            isBookmark = !info.isBookmark
        ))

        val msg = if (!info.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    fun handleShare() {
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    fun handleSearchQuery(query: String) {
        if (currentState.isSearch && currentState.searchQuery != query)
            updateState {
                it.copy(searchQuery = query)
            }
    }

    fun handleSearchPanel(open: Boolean) {
        if (currentState.isSearch != open)
            updateState { it.copy(isSearch = open) }
    }

}

data class ArticleState (
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
    val searchResult: List<Pair<Int, Int>> = emptyList(),
    val searchPosition: Int = 0,
    val shareLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val author: Any? = null,
    val poster: String? = null,
    val content: List<Any> = emptyList(),
    val reviews: List<Any> = emptyList()
)
