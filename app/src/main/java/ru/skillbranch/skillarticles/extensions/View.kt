package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop


fun View.setMarginOptionally(left:Int = marginLeft, top : Int = marginTop, right : Int = marginRight, bottom : Int = marginBottom) {
    val lp = layoutParams as? MarginLayoutParams
    lp?.apply {
        setMargins(left, top, right, bottom)
    }
    layoutParams = lp
}

fun View.setPaddingOptionally(left:Int = paddingLeft, top : Int = paddingTop, right : Int = paddingRight, bottom : Int = paddingBottom) {
    setPadding(left, top, right, bottom)
}