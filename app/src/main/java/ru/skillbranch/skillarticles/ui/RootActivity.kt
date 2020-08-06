package ru.skillbranch.skillarticles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity() {

    private lateinit var viewModel: ArticleViewModel
    private var searchView: SearchView? = null
    private var logo: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupToolbar()
        setupBottombar()
        setupSubmenu()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this) {
            renderUi(it)
        }

        viewModel.observeNotifications(this) {
            renderNotifiation(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        searchView = menu.findItem(R.id.action_search)?.actionView as? SearchView
        searchView?.run {
            isIconified = !viewModel.currentState.isSearch
            if (viewModel.currentState.isSearch)
                setQuery(viewModel.currentState.searchQuery ?: "", false)

            setOnCloseListener { true }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.handleSearchQuery(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.handleSearchQuery(newText)
                    return true
                }
            })
        }
        if (viewModel.currentState.isSearch)
            menuItem.expandActionView()

        menuItem?.run {
            setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    viewModel.handleSearchPanel(true)
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    viewModel.handleSearchPanel(false)
                    return true
                }

            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    @Override
    override fun onBackPressed() {
        searchView?.takeIf { it.isIconified } ?.onActionViewCollapsed()
            ?: super.onBackPressed()
    }

    private fun renderNotifiation(notify: Notify) {
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)

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

    private fun setupSubmenu() {
        btn_text_up.setOnClickListener { viewModel.handleUpText() }
        btn_text_down.setOnClickListener { viewModel.handleDownText() }
        switch_mode.setOnClickListener { viewModel.handleNightMode() }
    }

    private fun setupBottombar() {
        btn_like.setOnClickListener { viewModel.handleLike() }
        btn_bookmark.setOnClickListener { viewModel.handleBookmark() }
        btn_share.setOnClickListener { viewModel.handleShare() }
        btn_settings.setOnClickListener { viewModel.handleToggleMenu() }

    }

        private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        logo = if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
        logo?.let { logo ->
            logo.scaleType = ImageView.ScaleType.CENTER_CROP
            val lp = logo.layoutParams as? Toolbar.LayoutParams
            lp?.let {
                it.width = this.dpToIntPx(40)
                it.height = this.dpToIntPx(40)
                it.marginEnd = this.dpToIntPx(16)
                logo.layoutParams = it
            }
        }
    }

    private fun renderUi(data: ArticleState) {
        btn_settings.isChecked = data.isShowMenu
        if (data.isShowMenu)
            submenu.open()
        else
            submenu.close()

        searchView?.apply {
            Log.d("searchView?.apply", "isIconified: $isIconified, data.isSearch: ${data.isSearch}")
            if (isIconified == data.isSearch) {
                isIconified = !data.isSearch
            }
//            logo?.visibility = if (data.isSearch) View.GONE else View.VISIBLE
            if (data.searchQuery != null && query.toString() != data.searchQuery)
                setQuery(data.searchQuery, false)
        }


        btn_like.isChecked = data.isLike
        btn_bookmark.isChecked = data.isBookmark

        switch_mode.isChecked = data.isDarkMode
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (data.isBigText) {
            tv_text_content.textSize = 18f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        } else {
            tv_text_content.textSize = 14f
            btn_text_up.isChecked = false
            btn_text_down.isChecked = true
        }

        tv_text_content.text = if (data.isLoadingContent) "loading" else data.content.first() as String

        toolbar.title = data.title ?: "loading"
        toolbar.subtitle = data.category ?: "loading"
        if (data.categoryIcon != null)
            toolbar.logo = getDrawable(data.categoryIcon as Int)
    }

}