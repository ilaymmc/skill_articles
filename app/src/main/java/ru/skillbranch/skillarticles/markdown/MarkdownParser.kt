package ru.skillbranch.skillarticles.markdown

import java.lang.annotation.ElementType
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
    private const val CODE_GROUP = "((?<!\\`)\\`\\`\\`[^\\`\\s]\\S[^\\`]+[^\\`\\s]\\`\\`\\`(?!\\`))"
    private const val ORDERED_LIST_GROUP = "(^\\d+?\\. .+\$)"

    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP|$CODE_GROUP|$ORDERED_LIST_GROUP"
    private val elementsPattern by lazy {
        Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE)
    }

    fun parse(string: String) : MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(findElement(string))
        return MarkdownText(elements)
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
                parents.add(Element.Text(string.subSequence(lastStartIndex,startIndex)))
            }

            var text: CharSequence

            val groups = 1..12
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
                    val lines = string.subSequence(startIndex.plus(3), endIndex.minus(3)).split(LINE_SEPARATOR)
                    val l = lines.size
                    if (l == 1) {
                        parents.add(Element.BlockCode(Element.BlockCode.Type.SINGLE,lines[0]))
                    } else {
                        lines.mapIndexed { i, el ->
                            val element =
                                Element.BlockCode(
                                    when(i) {
                                        0 -> Element.BlockCode.Type.START
                                        l-1 -> Element.BlockCode.Type.END
                                        else -> Element.BlockCode.Type.MIDDLE
                                    },
                                    el + if (i < l - 1) LINE_SEPARATOR else ""
                                )
                            parents.add(element)
                        }
                    }
                    lastStartIndex = endIndex
                }
                // numbered list
                11 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (order, title) = "^(\\d+?)\\. (.+)\$".toRegex().find(text)!!.destructured
                    val subs = findElement(title)
                    val element = Element.OrderedListItem(order, title, subs)
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
        val type: Type,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type {
            SINGLE,
            START,
            MIDDLE,
            END
        }
    }

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()
}