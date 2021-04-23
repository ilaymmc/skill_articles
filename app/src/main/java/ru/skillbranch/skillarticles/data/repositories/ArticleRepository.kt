package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.*
import ru.skillbranch.skillarticles.data.models.*
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.RestService
import ru.skillbranch.skillarticles.data.remote.req.MessageReq
import ru.skillbranch.skillarticles.data.remote.res.CommentRes
import ru.skillbranch.skillarticles.extensions.data.toArticleContent


interface IArticleRepository {
    fun findArticle(articleId: String): LiveData<ArticleFull>
    fun getAppSettings(): LiveData<AppSettings>
    suspend fun toggleLike(articleId: String)
    suspend fun toggleBookmark(articleId: String)
    fun isAuth(): LiveData<Boolean>
    suspend fun sendMessage(articleId: String, comment: String, answerToMessage: String?)
    fun loadAllComments(articleId: String, totalCount: Int, errorHandler: (Throwable) -> Unit): CommentsDataFactory
    suspend fun decrementLike(articleId: String)
    suspend fun incrementLike(articleId: String)
    fun updateSettings(copy: AppSettings)
    suspend fun fetchArticleContent(articleId: String)
    fun findArticleCommentCount(articleId: String): LiveData<Int>
}


object ArticleRepository : IArticleRepository {
    private val network = NetworkManager.api
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
    override suspend fun toggleLike(articleId: String) {
        articlePersonalDao.toggleLikeOrInsert(articleId)
    }

    override suspend fun toggleBookmark(articleId: String) {
        articlePersonalDao.toggleBookmarkOrInsert(articleId)
    }

    override suspend fun fetchArticleContent(articleId: String) {
        val content = network.loadArticleContent(articleId)
        articleContentsDao.insert(content.toArticleContent())
    }

    override fun findArticleCommentCount(articleId: String): LiveData<Int> {
        return articleCountsDao.getCommentsCount(articleId)

    }

    suspend fun updateArticlePersonalInfo(info: ArticlePersonalInfo) {
        articlePersonalDao.update(info)
    }

    override fun isAuth(): LiveData<Boolean> = preferences.isAuthLive
    override fun loadAllComments(articleId: String, totalCount: Int, errorHandler: (Throwable) -> Unit) =
        CommentsDataFactory(
            itemProvider = network,
            articleId = articleId,
            totalCount = totalCount,
            errorHandler = errorHandler
        )

    override suspend fun decrementLike(articleId: String) {
        if (preferences.accessToken.isEmpty()) {
            articleCountsDao.decrementLike(articleId)
            return
        }
        try {
            val res = network.decrementLike(articleId, preferences.accessToken)
            articleCountsDao.updateLike(articleId, res.likeCount)
        } catch (e: Throwable) {
            articleCountsDao.decrementLike(articleId)

        }
    }

    override suspend fun incrementLike(articleId: String) {
        if (preferences.accessToken.isEmpty()) {
            articleCountsDao.incrementLike(articleId)
            return
        }
        try {
            val res = network.incrementLike(articleId, preferences.accessToken)
            articleCountsDao.updateLike(articleId, res.likeCount)
        } catch (e: Throwable) {
            articleCountsDao.incrementLike(articleId)
        }
    }

    override suspend fun sendMessage(articleId: String, comment: String, answerToMessageId: String?) {
        val (_, messageCount) = network.sendMessage(
            articleId, MessageReq(comment, answerToMessageId), preferences.accessToken)
        articleCountsDao.updateCommentsCount(articleId, messageCount)
    }

    suspend fun refreshCommentsCount(articleId: String) {
//        val counts = network.loadArticleCounts(articleId)
//        articleCountsDao.updateCommentsCount(articleId, counts.comments)
    }
}

class CommentsDataFactory(
    private val itemProvider: RestService,
    private val articleId: String,
    private val totalCount: Int,
    private val errorHandler: (Throwable) -> Unit
) : DataSource.Factory<String?, CommentRes>() {
    override fun create(): DataSource<String?, CommentRes> =
        CommentsDataSource(itemProvider, articleId, totalCount, errorHandler)

}

class CommentsDataSource(
    private val itemProvider: RestService,
    private val articleId: String,
    private val totalCount: Int,
    private val errorHandler: (Throwable) -> Unit
) : ItemKeyedDataSource<String, CommentRes>() {

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<CommentRes>
    ) {
        try {
            val result =
                itemProvider.loadComments(
                    articleId,
                    params.requestedInitialKey,
                    params.requestedLoadSize
                ).execute()

            callback.onResult(
                if (totalCount > 0) result.body()!! else emptyList(),
                0,
                totalCount
            )
        } catch (e: Throwable) {
            errorHandler(e)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<CommentRes>) {
        try {
            val result = itemProvider.loadComments(
                articleId,
                params.key,
                params.requestedLoadSize
            ).execute()
            callback.onResult(result.body()!!)
        } catch (e: Throwable) {
            errorHandler(e)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<CommentRes>) {
        try {
            val result = itemProvider.loadComments(
                articleId,
                params.key,
                -params.requestedLoadSize
            ).execute()
            callback.onResult(result.body()!!)
        } catch (e: Throwable) {
            errorHandler(e)
        }
    }

    override fun getKey(item: CommentRes): String = item.id
}
