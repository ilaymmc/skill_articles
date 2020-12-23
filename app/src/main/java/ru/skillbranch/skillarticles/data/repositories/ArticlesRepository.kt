package ru.skillbranch.skillarticles.data.repositories

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PositionalDataSource
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.models.CommentItemData
import java.lang.Thread.sleep

object ArticlesRepository {

    private val local = LocalDataHolder
    private val network = NetworkDataHolder

    fun allArticles(): ArticlesDataFactory =
        ArticlesDataFactory(ArticlesStrategy.AllArticles(::findArticleByRange))

    fun searchArticles(searchQuery: String): ArticlesDataFactory =
        ArticlesDataFactory(ArticlesStrategy.SearchArticles(::findArticleByTitle, searchQuery))

//    fun bookmarkedArticles(): ArticlesDataFactory =
//        ArticlesDataFactory(ArticlesStrategy.BookmarkedArticles(::findBookmarkedArticles))

    fun bookmarkedArticles(): ArticlesDataFactory =
        ArticlesDataFactory(ArticlesStrategy.AllArticles(::findBookmarkedArticles))

    fun findBookmarkedArticles(start: Int, size: Int) =
        local.localArticleItems
            .asSequence()
            .filter { it.isBookmark }
            .drop(start)
            .take(size)
            .toList()

    fun findArticleByTitle(start: Int, size: Int, searchTitle: String) =
        local.localArticleItems
            .asSequence()
            .filter { it.title.contains(searchTitle, true) }
            .drop(start)
            .take(size)
            .toList()

    fun findArticleByRange(start: Int, size: Int) =
        local.localArticleItems
            .drop(start)
            .take(size)

    fun loadArticlesFromNetwork(start: Int, size: Int) : List<ArticleItemData> =
        network.networkArticleItems
            .drop(start)
            .take(size)
            .apply { sleep(500) }

    fun insertArticlesToDd(articles : List<ArticleItemData>) {
        local.localArticleItems.addAll(articles)
            .apply { sleep(500) }
    }

    fun updateBookmark(id: String, isChecked: Boolean) {
        val index = local.localArticleItems.indexOfFirst { it.id == id }
        local.localArticleItems[index] = local.localArticleItems[index].copy( isBookmark = isChecked )
    }
}

class ArticlesDataFactory(val strategy: ArticlesStrategy) : DataSource.Factory<Int, ArticleItemData>() {
    override fun create(): DataSource<Int, ArticleItemData> = ArticlesDataSource(strategy)

}

class ArticlesDataSource(private val strategy: ArticlesStrategy) : PositionalDataSource<ArticleItemData>() {
    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<ArticleItemData>
    ) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        Log.e(
            "ArticleRepository",
            "loadInitial: start = ${params.requestedStartPosition}, size = ${params.requestedLoadSize}, resultSize = ${result.size}")
        callback.onResult(result, params.requestedStartPosition)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItemData>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        Log.e("ArticleRepository",
            "loadRange: start = ${params.startPosition}, size = ${params.loadSize}, resultSize = ${result.size}")
        callback.onResult(result)
    }

}

sealed class ArticlesStrategy {
    abstract fun getItems(start: Int, size: Int) : List<ArticleItemData>

    class AllArticles(
        private val itemProvider : (Int, Int) -> List<ArticleItemData>
    ): ArticlesStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> = itemProvider(start, size)
    }
    class SearchArticles(
        private val itemProvider : (Int, Int, String) -> List<ArticleItemData>,
        private val query: String
    ): ArticlesStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> = itemProvider(start, size, query)
    }

    class BookmarkedArticles(
        private val itemProvider : (Int, Int) -> List<ArticleItemData>
    ): ArticlesStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> = itemProvider(start, size)
    }

}