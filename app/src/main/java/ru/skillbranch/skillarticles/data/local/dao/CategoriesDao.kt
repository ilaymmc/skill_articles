package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.data.local.entities.Category

@Dao
interface CategoriesDao: BaseDao<Category> {

    @Query("""
        SELECT category.title as title, category.icon as icon, category.category_id as category_id, 
        count(article.id) as articles_count 
        FROM article_categories as category 
        INNER JOIN articles as article on category.category_id = article.category_id
        GROUP BY category.category_id
        ORDER BY articles_count DESC 
    """)
    fun findAllCategoriesData() : LiveData<List<CategoryData>>
}