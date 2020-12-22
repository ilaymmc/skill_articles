package ru.skillbranch.skillarticles.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Date.format(pattern: String = "HH:mm:ss dd.MM.yy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}
enum class TimeUnits(val microSeconds: Int) {
    SECOND(1000),
    MINUTE(1000 * 60),
    HOUR(1000 * 60 * 60),
    DAY(1000 * 60 * 60 * 24)
}
fun Date.add(value: Int, units: TimeUnits) : Date = Date(this.time + value * units.microSeconds)

fun Date.humanizeDiff(): String {
    return this.format()
}
