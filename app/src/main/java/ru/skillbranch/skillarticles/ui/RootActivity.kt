package ru.skillbranch.skillarticles.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
//import android.widget.SearchView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setMarignOptionaly
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.ui.custom.SearchSpan
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelFactory


class RootActivity : BaseActivity<ArticleViewModel>(), IArticleView {

    override lateinit var viewModel: ArticleViewModel
    private var logo: ImageView? = null
    private var searchQuery: String? = null
    private var isSearching = false

    override val layout: Int = R.layout.activity_root

    override fun setupViews() {
        setupToolbar()
        setupBottombar()
        setupSubmenu()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val vmFactory =
            ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this) {
            renderUi(it)
        }

        viewModel.observeNotifications(this) {
            renderNotifiation(it)
        }
    }

//    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        searchView = menu.findItem(R.id.action_search)?.actionView as? SearchView
//        searchView?.run {
////            isIconifiedByDefault = !viewModel.currentState.isSearch
//        }
//        return super.onPrepareOptionsMenu(menu)
//    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu?.findItem(R.id.action_search)
        val searchView = menuItem?.actionView as? SearchView
        searchView?.queryHint = getString(R.string.article_search_placeholder)

        if (isSearching) {
            menuItem?.expandActionView()
            searchView?.setQuery(searchQuery, false)
            searchView?.clearFocus()
        }

        menuItem?.run {
            setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    viewModel.handleSearchModel(true)
                    return true
                }
                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    viewModel.handleSearchModel(false)
                    return true
                }

            })
        }
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })
//        val searchEdit =
//            searchView?.findViewById<View>(R.id.search_src_text)
//
//        val searchEdit1 =
//            searchView?.findViewById<View>(androidx.appcompat.R.id.search_src_text)
//
//        val searchEdit2 =
//            searchView?.findViewById<View>(searchId)

        return super.onCreateOptionsMenu(menu)
    }

    @Override
    override fun onBackPressed() {
//        searchView?.takeIf { it.isIconified } ?.onActionViewCollapsed()
//            ?: super.onBackPressed()
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

        btn_result_up.setOnClickListener {
            if (search_view.hasFocus())
                search_view.clearFocus()
            viewModel.handleUpResult()
        }

        btn_result_down.setOnClickListener {
            if (search_view.hasFocus())
                search_view.clearFocus()
            viewModel.handleDownResult()
        }

        btn_search_close.setOnClickListener {
            viewModel.handleSearchModel(false)
            invalidateOptionsMenu()
        }

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

        if (data.isSearch)
            showSearchBar()
        else
            hideSearchBar()

        if (data.searchResult.isNotEmpty())
            renderSearchResult(data.searchResult)

        btn_settings.isChecked = data.isShowMenu
        if (data.isShowMenu)
            submenu.open()
        else
            submenu.close()

//        searchView?.apply {
//            Log.d("searchView?.apply", "isIconified: $isIconified, data.isSearch: ${data.isSearch}")
//            if (isIconified == data.isSearch) {
//                isIconified = !data.isSearch
//            }
////            logo?.visibility = if (data.isSearch) View.GONE else View.VISIBLE
//            if (data.searchQuery != null && query.toString() != data.searchQuery)
//                setQuery(data.searchQuery, false)
//        }


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

        if (data.isLoadingContent) {
            tv_text_content.text = "loading"
        } else if (tv_text_content.text == "loading") {
            val content = data.content.first() as String
            tv_text_content.setText(content, TextView.BufferType.SPANNABLE)
        }

        toolbar.title = data.title ?: "loading"
        toolbar.subtitle = data.category ?: "loading"
        if (data.categoryIcon != null)
            toolbar.logo = getDrawable(data.categoryIcon as Int)
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        val content = tv_text_content.text as Spannable

        val bgColor = Color.RED
        val fgColor = Color.WHITE

        searchResult.forEach { (start, end) ->
            content.setSpan(
                SearchSpan(bgColor, fgColor),
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun renderSearchPosition(searchPosition: Int) {
        TODO("Not yet implemented")
    }

    override fun clearSearchResult() {
        TODO("Not yet implemented")
    }

    override fun showSearchBar() {
        bottombar.setSearchState(true)
        scroll.setMarignOptionaly(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        bottombar.setSearchState(false)
        scroll.setMarignOptionaly(bottom = 0)
    }

}