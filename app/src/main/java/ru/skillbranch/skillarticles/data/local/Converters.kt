package ru.skillbranch.skillarticles.data.local

import androidx.room.TypeConverter
import ru.skillbranch.skillarticles.data.local.entities.ArticleTagXRef
import ru.skillbranch.skillarticles.data.local.entities.Tag
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.data.repositories.MarkdownParser
import java.util.Date

class DateConverter {
    @TypeConverter
    fun timestampToDate(timestamp : Long) : Date = Date(timestamp)

    @TypeConverter
    fun dateToTimestamp(date: Date) : Long = date.time
}

class MarkdownConverter {
    @TypeConverter
    fun toMarkdown(content: String?): List<MarkdownElement>? = content?.let { MarkdownParser.parse(it) }
}

//class TagConverter {
//    @TypeConverter
//    fun articleTagToString(tag: ArticleTagXRef): String = tag.tagId
//    @TypeConverter
//    fun tagToString(tag: Tag): String = tag.tag
//}