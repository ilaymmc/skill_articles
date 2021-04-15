package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.*
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.res.ArticleRes
import ru.skillbranch.skillarticles.extensions.data.toArticle
import ru.skillbranch.skillarticles.extensions.data.toArticleCounts
import ru.skillbranch.skillarticles.extensions.data.toCategory
import kotlin.reflect.jvm.internal.impl.types.AbstractTypeCheckerContext

interface IArticlesRepository {
    suspend fun loadArticlesFromNetwork(start: String? = null, size: Int = 10) : Int
    suspend fun insertArticlesToDb(articles: List<ArticleRes>)
    suspend fun toggleBookmark(articleId: String)
    fun findTags() : LiveData<List<String>>
    fun findCategoriesData() : LiveData<List<CategoryData>>
    fun rawQueryArticles(filter: ArticleFilter): DataSource.Factory<Int, ArticleItem>
    suspend fun incrementTagUseCount(tag: String)
}

object ArticlesRepository : IArticlesRepository {
    private val network = NetworkManager.api
    private var articlesDao = db.articlesDao()
    private var articleCountsDao = db.articleCountsDao()
    private var categoriesDao = db.categoriesDao()
    private var tagsDao = db.tagsDao()
    private var articlePersonalDao = db.articlePersonalInfosDao()

    fun setupTestDao (
        articlesDao : ArticlesDao? = null,
        articleCountsDao: ArticleCountsDao? = null,
        categoriesDao: CategoriesDao? = null,
        tagsDao: TagsDao? = null,
        articlePersonalDao: ArticlePersonalInfosDao? = null
    ) {
        articlesDao?.let { this.articlesDao = it }
        articleCountsDao?.let { this.articleCountsDao = it }
        categoriesDao?.let { this.categoriesDao = it }
        tagsDao?.let { this.tagsDao = it }
        articlePersonalDao?.let { this.articlePersonalDao = it }
    }
    override suspend fun loadArticlesFromNetwork(start: String?, size: Int): Int = withContext(Dispatchers.IO) {
        val items = network.articles(start, size)
        if (items.isNotEmpty())
            insertArticlesToDb(items)
        items.size
    }

    override suspend fun insertArticlesToDb(articles: List<ArticleRes>) = withContext(Dispatchers.IO) {
        articlesDao.upsert(articles.map { it.data.toArticle() })
        articleCountsDao.upsert(articles.map { it.counts.toArticleCounts() })
        val refs = articles.map { it.data }
//            .flatMap { res -> res.tags.map { res.id to it } }
            .fold(mutableListOf<Pair<String, String>>()) { acc, res ->
                acc.also { list -> list.addAll(res.tags.map {res.id to it} ) }
            }

        val tags = refs.map { it.second }
            .distinct()
            .map { Tag(it) }

        val categories = articles.map { it.data.category.toCategory() }

        categoriesDao.insert(categories)
        tagsDao.insert(tags)
        tagsDao.insertRefs(refs.map { ArticleTagXRef(it.first, it.second) })
        Unit
    }

    override suspend fun toggleBookmark(articleId: String) {
        articlePersonalDao.toggleBookmarkOrInsert(articleId)
    }

    override fun findTags(): LiveData<List<String>> =
        tagsDao.findTags()

    override fun findCategoriesData(): LiveData<List<CategoryData>> =
        categoriesDao.findAllCategoriesData()

    fun findPersonalData(): LiveData<List<ArticlePersonalInfo>> =
        articlePersonalDao.findPersonalInfos()

    override fun rawQueryArticles(filter: ArticleFilter): DataSource.Factory<Int, ArticleItem> =
        articlesDao.findArticlesByRaw(SimpleSQLiteQuery(filter.toQuery()))

    override suspend fun incrementTagUseCount(tag: String) {
        tagsDao.incrementTagUseCount(tag)
    }

    suspend fun findLastArticleId(): String? = withContext(Dispatchers.IO) { articlesDao.findLastArticleId() }
}

class ArticleFilter(
    val search: String? = null,
    val isBookmark: Boolean = false,
    val categories: List<String> = listOf(),
    val isHashtag: Boolean = false
) {
    fun toQuery(): String {
        val qb = QueryBuilder()
        qb.table("ArticleItem")
        if (search != null && !isHashtag)
            qb.appendWhere("title LIKE '%$search%'")
        if (search != null && isHashtag) {
            qb.innerJoin("article_tag_x_ref AS refs", "refs.a_id = id")
            qb.appendWhere("refs.t_id = '$search'")
        }
        if (isBookmark)
            qb.appendWhere("is_bookmark = 1")
        if (categories.isNotEmpty())
            qb.appendWhere("category_id IN (${categories.joinToString(",")})")
        qb.orderBy("date")
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

    fun table(table: String) : QueryBuilder {
        this.table = table
        return this
    }

    fun orderBy(column: String, isDesc: Boolean = true) : QueryBuilder {
        this.order = "ORDER BY $column ${if(isDesc) "DESC" else "ASC"}"
        return this
    }

    fun appendWhere(condition: String, logic: String = "AND") : QueryBuilder {
        if (whereCondition.isNullOrEmpty()) {
            whereCondition = "WHERE $condition "
        } else {
          whereCondition += "$logic $condition "
        }
        return this
    }
    fun innerJoin(table: String, on: String) : QueryBuilder  {
        if (joinTables.isNullOrEmpty()) joinTables = ""
        joinTables += "INNER JOIN $table ON $on "

        return this
    }

    fun build ():String {
        check(table != null) { "table must not be null" }
        val strBuilder = StringBuilder("SELECT ")
            .append("$selectColumn ")
            .append("FROM $table ")

        if (joinTables != null) {
            strBuilder.append(joinTables)
        }
        if (whereCondition != null)
            strBuilder.append(whereCondition)

        if (order != null)
            strBuilder.append(order)

        return strBuilder.toString()
    }


}