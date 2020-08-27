package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
    this?.takeIf { it.isNotEmpty() } ?: return emptyList()
    substr.takeIf { it.isNotEmpty() } ?: return emptyList()

    return generateSequence(-1) { pos ->
            val newPos = indexOf(substr, pos + 1, ignoreCase)
            if (newPos in 0 until length) {
                newPos
            } else {
                null
            }
        }.drop(1).toList()
}