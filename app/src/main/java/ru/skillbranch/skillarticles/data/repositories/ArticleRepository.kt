package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.*
import ru.skillbranch.skillarticles.data.models.*
import ru.skillbranch.skillarticles.extensions.data.toArticleContent
import java.lang.Thread.sleep
import kotlin.math.abs


interface IArticleRepository {
    fun findArticle(articleId: String): LiveData<ArticleFull>
    fun getAppSettings(): LiveData<AppSettings>
    fun toggleLike(articleId: String)
    fun toggleBookmark(articleId: String)
    fun isAuth(): LiveData<Boolean>
    fun loadCommentsByRange(slug: String?, size: Int, articleId: String): List<CommentItemData>
    fun sendMessage(articleId: String, comment: String, answerToSlug: String?)
    fun loadAllComments(articleId: String, totalCount: Int): CommentsDataFactory
    fun decrementLike(articleId: String)
    fun incrementLike(articleId: String)
    fun updateSettings(copy: AppSettings)
    fun fetchArticleContent(articleId: String)
    fun findArticleCommentCount(articleId: String): LiveData<Int>
}


object ArticleRepository : IArticleRepository {
    private val network = NetworkDataHolder
    private val preferences = PrefManager
    private var articlesDao = db.articlesDao()
    private var articlePersonalDao = db.articlePersonalInfosDao()
    private var articleCountsDao = db.articleCountsDao()
    private var articleContentsDao = db.articleContentsDao()
    private var categoriesDao = db.categoriesDao()
    private var tagsDao = db.tagsDao()

    fun setupTestDao (
        articlesDao : ArticlesDao? = null,
        articleCountsDao: ArticleCountsDao? = null,
        articleContentDao: ArticleContentsDao? = null,
        articlePersonalDao: ArticlePersonalInfosDao? = null
    ) {
        articlesDao?.let { this.articlesDao = it }
        articleCountsDao?.let { this.articleCountsDao = it }
        articleContentDao?.let { this.articleContentsDao = it }
        articlePersonalDao?.let { this.articlePersonalDao = it }
    }
    override fun findArticle(articleId: String): LiveData<ArticleFull> {
        return articlesDao.findFullArticle(articleId)
    }

    override fun getAppSettings(): LiveData<AppSettings> = preferences.appSettings //from preferences
    override fun updateSettings(copy: AppSettings) {
        preferences.isBigText = copy.isBigText
        preferences.isDarkMode = copy.isDarkMode
    }
    override fun toggleLike(articleId: String) {
        articlePersonalDao.toggleLikeOrInsert(articleId)
    }

    override fun toggleBookmark(articleId: String) {
        articlePersonalDao.toggleBookmarkOrInsert(articleId)
    }

    override fun fetchArticleContent(articleId: String) {
        val content = network.loadArticleContent(articleId).apply { sleep(1500) }
        articleContentsDao.insert(content.toArticleContent())

    }

    override fun findArticleCommentCount(articleId: String): LiveData<Int> {
        return articleCountsDao.getCommentsCount(articleId)

    }

    fun updateArticlePersonalInfo(info: ArticlePersonalInfo) {
        articlePersonalDao.update(info)
    }

    override fun isAuth(): LiveData<Boolean> = preferences.isAuthLive
    override fun loadAllComments(articleId: String, totalCount: Int) =
        CommentsDataFactory(
            itemProvider = ::loadCommentsByRange,
            articleId = articleId,
            totalCount = totalCount
        )

    override fun loadCommentsByRange(
        slug: String?,
        size: Int,
        articleId: String
    ): List<CommentItemData> {
        val data = network.commentsData.getOrElse(articleId) { mutableListOf() }
        return when {
            slug == null -> data.take(size)

            size > 0 -> data.dropWhile { it.slug != slug }
                .drop(1)
                .take(size)

            size < 0 -> data
                .dropLastWhile { it.slug != slug }
                .dropLast(1)
                .takeLast(abs(size))

            else -> emptyList()
        }.apply { sleep(1500) }
    }

    override fun decrementLike(articleId: String) {
        articleCountsDao.decrementLike(articleId)
    }

    override fun incrementLike(articleId: String) {
        articleCountsDao.incrementLike(articleId)
    }

    override fun sendMessage(articleId: String, comment: String, answerToSlug: String?) {
        network.sendMessage(
            articleId, comment, answerToSlug,
            User("777", "John Doe", "https://skill-branch.ru/img/mail/bot/android-category.png")
        )
        articleCountsDao.incrementCommentsCount(articleId)
    }
}

class CommentsDataFactory(
    private val itemProvider: (String?, Int, String) -> List<CommentItemData>,
    private val articleId: String,
    private val totalCount: Int
) : DataSource.Factory<String?, CommentItemData>() {
    override fun create(): DataSource<String?, CommentItemData> =
        CommentsDataSource(itemProvider, articleId, totalCount)

}

class CommentsDataSource(
    private val itemProvider: (String?, Int, String) -> List<CommentItemData>,
    private val articleId: String,
    private val totalCount: Int
) : ItemKeyedDataSource<String, CommentItemData>() {

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<CommentItemData>
    ) {
        val result = itemProvider(params.requestedInitialKey, params.requestedLoadSize, articleId)

        callback.onResult(
            if (totalCount > 0) result else emptyList(),
            0,
            totalCount
        )
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<CommentItemData>) {
        val result = itemProvider(params.key, params.requestedLoadSize, articleId)
        callback.onResult(result)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<CommentItemData>) {
        val result = itemProvider(params.key, -params.requestedLoadSize, articleId)
        callback.onResult(result)
    }

    override fun getKey(item: CommentItemData): String = item.slug

}
