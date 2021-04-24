package ru.skillbranch.skillarticles.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.skillbranch.skillarticles.data.local.entities.ArticleContent

@Dao
interface ArticleContentsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: ArticleContent):Long

    @Query("SELECT * FROM article_contents")
    suspend fun findArticlesContentsTest(): List<ArticleContent>

    @Query("DELETE FROM article_contents WHERE article_id = :articleId")
    suspend fun remove(articleId: String)

}