package ru.skillbranch.skillarticles.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.viewmodels.RootState
import ru.skillbranch.skillarticles.viewmodels.RootViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify


class RootActivity : BaseActivity<RootViewModel>() {

    public override val viewModel: RootViewModel by viewModels()

    private var logo: ImageView? = null
    private var inNavViewSelected = false
    private var isAuth = false

    override val layout: Int = R.layout.activity_root

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

        val appbarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_articles,
                R.id.nav_bookmarks,
                R.id.nav_transcriptions,
                R.id.nav_profile
            )
        )

        setupActionBarWithNavController(navController, appbarConfiguration)
//        nav_view.setupWithNavController(navController)
        nav_view.setOnNavigationItemSelectedListener {
            viewModel.navigate(NavigationCommand.To(it.itemId))
            true
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
//            Log.e("RootActivity", "addOnDestination: ${destination.id}")
            if (destination.id == R.id.nav_auth) {
                if (isAuth) {
                    controller.popBackStack()
                    arguments?.get("private_destination")?.let {
                        navController.navigate(it as Int)
                    }
                }
            }else
                nav_view.selectDestination(destination)
        }
    }

    override fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(container, notify.message, Snackbar.LENGTH_LONG)

        if (bottombar != null)
            snackbar.anchorView = bottombar
        else
            snackbar.anchorView = nav_view

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

    override fun subscribeOnState(state: IViewModelState) {
        if (state is RootState)
            isAuth = state.isAuth
    }

}

// Реализуй BottomNavigationView.selectDestination(destination: NavDestination)
// для отображения текущего пункта меню BottomNavigationView соответствующему NavDestination.
// Если destination является потомком destination top уровня
// (@+id/nav_articles, @+id/nav_profile, @+id/nav_bookmarks, @+id/nav_transcriptions)
// то соответствующий пункт меню в BottomNavigationView должен быть в состоянии selected
private fun BottomNavigationView.selectDestination(destination: NavDestination) {
    var dest : NavDestination? = destination
    while (dest != null) {
        val item = menu.findItem(destination.id)
        item?.let {
            if (!item.isChecked)
                item.isChecked = true
            return@selectDestination
        }
        dest = dest.parent
    }
}

