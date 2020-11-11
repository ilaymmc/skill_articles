package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.viewmodels.RootViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify


class RootActivity : BaseActivity<RootViewModel>() {

    override val viewModel: RootViewModel by viewModels()

    private var logo: ImageView? = null
//    private lateinit var prefManager: PrefManager

    override val layout: Int = R.layout.activity_root


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        prefManager = PrefManager(applicationContext)
        AppCompatDelegate.setDefaultNightMode(
//            if (prefManager.isDarkMode)
//                AppCompatDelegate.MODE_NIGHT_YES
//            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
//            .setAnchorView(bottombar)

        when(notify) {
            is Notify.TextMessage -> {}
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction((notify.actionLabel)) {
                    notify.actionHandler?.invoke()
                }
            }
            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }
        snackbar.show()

    }

    @Override
    override fun onBackPressed() {
//        searchView?.takeIf { it.isIconified } ?.onActionViewCollapsed()
//            ?: super.onBackPressed()
    }

    override fun subscribeOnState(state: IViewModelState) {
        // Not yet implemented
    }
}
