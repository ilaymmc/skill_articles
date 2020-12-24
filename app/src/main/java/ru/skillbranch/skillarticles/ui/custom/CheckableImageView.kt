package ru.skillbranch.skillarticles.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import android.widget.ImageView

@SuppressLint("AppCompatCustomView")
class CheckableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : ImageView(context, attrs, defStyleAttrs), Checkable, View.OnClickListener {

    private var _checked = false
    private var _onToggleListener : ((checked: Boolean) -> Unit)? = null

    companion object {
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    }
    init {
        setOnClickListener(this)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val states = super.onCreateDrawableState(extraSpace + 1)
        return if (isChecked)
            View.mergeDrawableStates(states, CHECKED_STATE_SET)
            else states
    }

    override fun isChecked(): Boolean = _checked

    override fun setChecked(checked: Boolean) {
        if (_checked != checked) {
            _checked = checked
            refreshDrawableState()
        }
    }

    override fun toggle() {
        isChecked = !_checked
    }


    override fun onClick(v: View?) {
        toggle()
        _onToggleListener?.let {
            it(isChecked)
        }
    }

    fun setOnToggleListener(listener: (checked: Boolean) -> Unit) {
        _onToggleListener = listener
    }

}