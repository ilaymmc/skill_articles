package ru.skillbranch.skillarticles.ui.custom.markdown

import android.text.Spannable
import androidx.core.text.getSpans
import ru.skillbranch.skillarticles.ui.custom.spans.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.spans.SearchSpan

interface IMarkdownView {
    var fontSize: Float
    val spannableContent: Spannable

    fun renderSearchResult(
        results: List<Pair<Int, Int>>,
        offset: Int
    ) {
        clearSearchResult()
        val offsetResult = results
            .map { (start, end) ->
                start - offset to end - offset
            }

        offsetResult.forEach {(start, end) ->
            spannableContent.setSpan(
                SearchSpan(),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

    }

    fun renderSearchPosition(
        searchPosition: Pair<Int, Int>,
        offset: Int
    ) {
        spannableContent.getSpans<SearchFocusSpan>().forEach {
            spannableContent.removeSpan(it)
        }
        spannableContent.setSpan(
            SearchFocusSpan(),
            searchPosition.first - offset,
            searchPosition.second - offset,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    fun clearSearchResult() {
        spannableContent.getSpans<SearchSpan>().forEach {
            spannableContent.removeSpan(it)
        }
    }
}