package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ru.skillbranch.skillarticles.data.local.entities.ArticleCounts
import ru.skillbranch.skillarticles.data.local.entities.ArticlePersonalInfo

@Dao
interface ArticlePersonalInfosDao: BaseDao<ArticlePersonalInfo> {

    @Transaction
    suspend fun upsert(list : List<ArticlePersonalInfo>) {
        insert(list)
            .mapIndexed { index, recordResult ->
                if (recordResult == -1L) list[index] else null
            }
            .filterNotNull()
            .also {
                if (it.isNotEmpty()) update(it)
            }

    }

    @Query("""
        UPDATE article_personal_infos SET is_like = NOT is_like, updated_at = CURRENT_TIMESTAMP 
        WHERE article_id = :articleId
    """)
    suspend fun toggleLike(articleId: String) : Int

    @Query("""
        UPDATE article_personal_infos SET is_bookmark = NOT is_bookmark, updated_at = CURRENT_TIMESTAMP 
        WHERE article_id = :articleId
    """)
    suspend fun toggleBookmark(articleId: String) : Int

    @Query("""
        SELECT is_bookmark FROM article_personal_infos  
        WHERE article_id = :articleId
    """)
    suspend fun getBookmark(articleId: String) : Boolean?

    @Query("""
        SELECT is_like FROM article_personal_infos  
        WHERE article_id = :articleId
    """)
    suspend fun isLike(articleId: String) : Boolean?

    @Transaction
    suspend fun toggleLikeOrInsert(articleId: String) : Boolean {
        if (toggleLike(articleId) == 0) insert(ArticlePersonalInfo(articleId = articleId, isLike = true))
        return isLike(articleId) ?: false
    }

    @Transaction
    suspend fun toggleBookmarkOrInsert(articleId: String): Boolean {
        if (toggleBookmark(articleId) == 0) insert(ArticlePersonalInfo(articleId = articleId, isBookmark = true))
        return getBookmark(articleId) ?: false
    }

    @Query("""
        UPDATE article_personal_infos SET is_bookmark = :bookmark, updated_at = CURRENT_TIMESTAMP 
        WHERE article_id = :articleId
    """)
    suspend fun updateBookmark(articleId: String, bookmark: Boolean) : Int

    @Transaction
    suspend fun addBookmarkOrInsert(articleId: String) {
        if (updateBookmark(articleId, true) == 0) insert(ArticlePersonalInfo(articleId = articleId, isBookmark = true))
    }

    @Transaction
    suspend fun removeBookmarkOrInsert(articleId: String) {
        if (updateBookmark(articleId, false) == 0) insert(ArticlePersonalInfo(articleId = articleId, isBookmark = false))
    }

    @Query("""
        SELECT * FROM article_personal_infos
    """)
    fun findPersonalInfos() : LiveData<List<ArticlePersonalInfo>>

    @Query("""
        SELECT * FROM article_personal_infos
        WHERE article_id = :articleId
    """)
    fun findPersonalInfos(articleId: String) : LiveData<ArticlePersonalInfo>


    @Query("SELECT * FROM article_personal_infos WHERE article_id = :articleId")
    suspend fun findPersonalInfosTest(articleId: String): ArticlePersonalInfo
}

