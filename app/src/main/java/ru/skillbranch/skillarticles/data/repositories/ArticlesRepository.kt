package ru.skillbranch.skillarticles.data.repositories

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import androidx.room.ColumnInfo
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.local.entitles.ArticleItem
import java.lang.Thread.sleep

object ArticlesRepository {

    private val local = LocalDataHolder
    private val network = NetworkDataHolder

    fun allArticles(): ArticlesDataFactory =
        ArticlesDataFactory(ArticlesStrategy.AllArticles(::findArticleByRange))

    fun searchArticles(searchQuery: String): ArticlesDataFactory =
        ArticlesDataFactory(ArticlesStrategy.SearchArticles(::findArticleByTitle, searchQuery))

    fun bookmarkedArticles(): ArticlesDataFactory =
        ArticlesDataFactory(ArticlesStrategy.BookmarkArticles(::findBookmarkedArticles))

    fun searchBookmarkedArticles(searchQuery: String): ArticlesDataFactory =
        ArticlesDataFactory(ArticlesStrategy.SearchBookmark(::findBookmarkedArticlesByTitle, searchQuery))

    private fun findBookmarkedArticles(start: Int, size: Int) =
        local.LOCAL_ARTICLE_ITEMS
            .asSequence()
            .filter { it.isBookmark }
            .drop(start)
            .take(size)
            .toList()

    private fun findBookmarkedArticlesByTitle(start: Int, size: Int, searchTitle: String) =
        local.LOCAL_ARTICLE_ITEMS
            .asSequence()
            .filter { it.title.contains(searchTitle, true) && it.isBookmark }
            .drop(start)
            .take(size)
            .toList()

    private fun findArticleByTitle(start: Int, size: Int, searchTitle: String) =
        local.LOCAL_ARTICLE_ITEMS
            .asSequence()
            .filter { it.title.contains(searchTitle, true) }
            .drop(start)
            .take(size)
            .toList()

    private fun findArticleByRange(start: Int, size: Int) =
        local.LOCAL_ARTICLE_ITEMS
            .drop(start)
            .take(size)

    fun loadArticlesFromNetwork(start: Int, size: Int) : List<ArticleItem> =
        network.NETWORK_ARTICLE_ITEMS
            .drop(start)
            .take(size)
            .apply { sleep(500) }

    fun insertArticlesToDd(articles : List<ArticleItem>) {
        local.LOCAL_ARTICLE_ITEMS.addAll(articles)
            .apply { sleep(500) }
    }

    fun updateBookmark(id: String, isChecked: Boolean) {
        val index = local.LOCAL_ARTICLE_ITEMS.indexOfFirst { it.id == id }
        local.LOCAL_ARTICLE_ITEMS[index] = local.LOCAL_ARTICLE_ITEMS[index].copy( isBookmark = isChecked )
    }
}

class ArticlesDataFactory(val strategy: ArticlesStrategy) : DataSource.Factory<Int, ArticleItem>() {
    override fun create(): DataSource<Int, ArticleItem> = ArticlesDataSource(strategy)
}

class ArticlesDataSource(private val strategy: ArticlesStrategy) : PositionalDataSource<ArticleItem>() {
    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<ArticleItem>
    ) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        Log.e(
            "ArticleRepository",
            "loadInitial: start = ${params.requestedStartPosition}, size = ${params.requestedLoadSize}, resultSize = ${result.size}")
        callback.onResult(result, params.requestedStartPosition)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItem>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        Log.e("ArticleRepository",
            "loadRange: start = ${params.startPosition}, size = ${params.loadSize}, resultSize = ${result.size}")
        callback.onResult(result)
    }

}

sealed class ArticlesStrategy {
    abstract fun getItems(start: Int, size: Int) : List<ArticleItem>

    class AllArticles(
        private val itemProvider : (Int, Int) -> List<ArticleItem>
    ): ArticlesStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItem> = itemProvider(start, size)
    }
    class SearchArticles(
        private val itemProvider : (Int, Int, String) -> List<ArticleItem>,
        private val query: String
    ): ArticlesStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItem> = itemProvider(start, size, query)
    }

    class BookmarkArticles(
        private val itemProvider : (Int, Int) -> List<ArticleItem>
    ): ArticlesStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItem> =
            itemProvider(start, size)
    }

     class SearchBookmark(
         private val itemProvider: (Int, Int, String) -> List<ArticleItem>,
         private val query: String
     ) : ArticlesStrategy() {
         override fun getItems(start: Int, size: Int): List<ArticleItem> =
             itemProvider(start, size, query)
     }

}

class ArticleItem(
    val search: String? = null,
    val isBookmark: Boolean = false,
    val categories: List<String> = listOf(),
    val isHashTag: Boolean = false
) {
    fun toQuery(): String {
        val qb = QueryBuilder()
        qb.table("ArticleItem")
        qb.orderBy("date")
        return  qb.build()

    }
}

class QueryBuilder() {
    private var table: String? = null
    private var selectColumn: String = "*"
    private var joinTables:String? = null
    private var whereCondition:String? = null
    private var order:String? = null

    fun build ():String {
        check(table != null) { "table must not be null" }
        val strBuilder = StringBuilder("SELECT ")
            .append("$selectColumn ")
            .append("FROM $table")

        return strBuilder.toString()
    }

    fun table(table: String) : QueryBuilder {
        this.table = table
        return this
    }

    fun orderBy(column: String, isDesc: Boolean = true) : QueryBuilder {
        this.order = "ORDER BY $column ${if(isDesc) "DESC" else "ASC"}"
        return this
    }

    // 01:59:40

}