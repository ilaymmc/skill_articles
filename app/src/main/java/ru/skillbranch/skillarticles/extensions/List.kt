package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {
    var idx = 0
    val result : MutableList<MutableList<Pair<Int, Int>>> = mutableListOf()
    bounds.forEach {bound ->
        val boundsGroup: MutableList<Pair<Int, Int>> = mutableListOf()
        while(idx < this.size && this[idx].second >= bound.first && this[idx].second <= bound.second) {
            boundsGroup.add(this[idx++])
        }
//        if (boundsGroup.isNotEmpty())
            result.add(boundsGroup)
    }
    return result
}