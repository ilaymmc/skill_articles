package ru.skillbranch.skillarticles.data.repositories

import java.lang.StringBuilder
import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP = "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?[^~]?~{2}(?!~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
//    private const val CODE_GROUP = "((?<!`)```[^`\\s]\\S[^`]+[^`\\s]```(?!`))"
    private const val CODE_GROUP = "(^```[^`\\s]\\S[^`]+[^`\\s]```$)"
    private const val ORDERED_LIST_GROUP = "(^\\d+?\\.\\s.+?$)"
    private const val IMAGE_GROUP = "(^!\\[[^\\[\\]]*?\\]\\(.*?\\)$)"

    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP|" +
            "$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP|" +
            "$CODE_GROUP|$ORDERED_LIST_GROUP|$IMAGE_GROUP"
    private val elementsPattern by lazy {
        Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE)
    }

    fun parse(string: String) : List<MarkdownElement> {
        val elements = mutableListOf<Element>()
        elements.addAll(findElement(string))
        return elements.fold(mutableListOf<MarkdownElement>()) { acc, element ->
            val last = acc.lastOrNull()
            when(element) {
                is Element.Image -> acc.add(MarkdownElement.Image(element, last?.bounds?.second ?: 0))
                is Element.BlockCode -> acc.add(MarkdownElement.Scroll(element, last?.bounds?.second ?: 0))
                else -> {
                    if (last is MarkdownElement.Text) last.elements.add(element)
                    else acc.add(MarkdownElement.Text(mutableListOf(element), last?.bounds?.second ?: 0))
                }
            }
            acc
        }
    }

    fun clear(string: String?) : String? {
        string ?: return null
        val elements = findElement(string)
        return clearElements(elements)
    }

    private fun clearElements(elements: List<Element>) : String {
        return elements.map {
            if (it.elements.isEmpty()) it.text
            else clearElements(it.elements)
        }.joinToString(separator = "")
    }

    private fun findElement(string: CharSequence) : List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        `while`@while(matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            if (lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            var text: CharSequence

            val groups = 1..13
            var group = -1

            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                -1 -> break@`while`

                // Unordered list
                1 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    val subs = findElement(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // Headers
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))

                    val level = reg!!.value.length

                    val text = string.subSequence(startIndex.plus(level.inc()), endIndex)
                    val element = Element.Header(level, text)
                    parents.add(element)

                    lastStartIndex = endIndex
                }

                // quote
                3 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    val subs = findElement(text)
                    val element = Element.Quote(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // italic
                4 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subs = findElement(text)
                    val element = Element.Italic(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // bold
                5 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))

                    val subs = findElement(text)
                    val element = Element.Bold(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // strike
                6 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))

                    val subs = findElement(text)
                    val element = Element.Strike(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // rule
                7 -> {
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // inline
                8 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // link
                9 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (title, link) = "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // code
                10 -> {
                    val text = string.subSequence(startIndex.plus(3), endIndex.minus(3))
                    parents.add(Element.BlockCode(text))
                    lastStartIndex = endIndex
                }
                // numbered list
                11 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (order, title) = "^(\\d+?\\.)\\s(.+)\$".toRegex().find(text)!!.destructured
                    val subs = findElement(title)
                    val element = Element.OrderedListItem(order, title, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // image
                12 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (alt, url, title) =
                        "^!\\[([^\\[\\]]*?)?]\\((.*?) \"(.*?)\"\\)$".toRegex().find(text)!!.destructured

                    val element = Element.Image(url, alt, title)
                    parents.add(element)
                    lastStartIndex = endIndex

                }
            }
        }
        if (lastStartIndex < string.length) {
            parents.add(Element.Text(string.subSequence(lastStartIndex, string.length)))
        }

        return parents

    }
}

data class MarkdownText(
    val elements: List<Element>
)

sealed class MarkdownElement {
    abstract val offset: Int

    val bounds: Pair<Int, Int> by lazy {
        when(this) {
            is Text -> {
                val end = elements.fold(offset) { acc, el ->
                    acc + el.spread().map { it.text.length }.sum()
                }
                offset to end
            }
            is Image -> offset to offset + image.text.length
            is Scroll -> offset to offset + code.text.length
        }
    }

    data class Text(
        val elements: MutableList<Element>,
        override val offset: Int = 0
    ): MarkdownElement()

    data class Image(
        val image: Element.Image,
        override val offset: Int = 0
    ): MarkdownElement()

    data class Scroll(
        val code: Element.BlockCode,
        override val offset: Int = 0
    ) : MarkdownElement()

}

sealed class Element {
    abstract val text : CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        var level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ",
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: CharSequence,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Image(
        val url: String,
        val atl: String?,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()
}

private fun Element.spread() : List<Element> {
    val elements = mutableListOf<Element>()
    if (this.elements.isNotEmpty())
        elements.addAll(this.elements.spread())
    else
        elements.add(this)
    return elements
}

private fun List<Element>.spread() : List<Element> =
    flatMap { it.spread() }

private fun Element.clearContent() : String =
    StringBuilder().apply {
        if (elements.isEmpty()) append(text)
        else elements.forEach { append(it.clearContent()) }
    }.toString()

fun List<MarkdownElement>.clearContent() : String =
    StringBuilder().apply {
        this@clearContent.forEach {
            when(it) {
                is MarkdownElement.Text -> it.elements.forEach { el -> append(el.clearContent()) }
                is MarkdownElement.Image -> append(it.image.clearContent())
                is MarkdownElement.Scroll -> append(it.code.clearContent())
            }
        }
    }.toString()
