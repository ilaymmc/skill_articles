package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.text.getSpans
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
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.custom.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.SearchSpan
import ru.skillbranch.skillarticles.ui.delegates.AttrValue
import ru.skillbranch.skillarticles.ui.delegates.ObserveProp
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelFactory


class RootActivity : BaseActivity<ArticleViewModel>(), IArticleView {

    override val binding: ArticleBinding by lazy {
        ArticleBinding()
    }

    override val viewModel: ArticleViewModel by lazy {
        val vmFactory = ViewModelFactory("0")
        ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
    }
    private var logo: ImageView? = null

    override val layout: Int = R.layout.activity_root

    private val searchSpanBgColor by AttrValue(R.attr.colorSecondary)
    private val searchSpanFgColor by AttrValue(R.attr.colorOnSecondary)

    override fun setupViews() {
        setupToolbar()
        setupBottombar()
        setupSubmenu()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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

        if (binding.isSearch) {
            menuItem?.expandActionView()
            searchView?.setQuery(binding.searchQuery, false)
            if (binding.isFocusedSearch)
                searchView?.requestFocus()
            else
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

    override fun renderNotification(notify: Notify) {
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
            if (search_view.hasFocus()) {
                search_view.clearFocus()
            }
            viewModel.handleUpResult()
            if (!tv_text_content.hasFocus()) {
                tv_text_content.requestFocus()
            }
        }

        btn_result_down.setOnClickListener {
            if (search_view.hasFocus()) {
                search_view.clearFocus()
            }
            viewModel.handleDownResult()
            if (!tv_text_content.hasFocus()) {
                tv_text_content.requestFocus()
            }
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

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        val content = tv_text_content.text as Spannable

        clearSearchResult()

        searchResult.forEach { (start, end) ->
            content.setSpan(
                SearchSpan(searchSpanBgColor, searchSpanFgColor),
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        renderSearchPosition(0)
    }

    override fun renderSearchPosition(searchPosition: Int) {
        val content = tv_text_content.text as Spannable


        val spans = content.getSpans<SearchSpan>()
        content.getSpans<SearchFocusSpan>().forEach {
            content.removeSpan(it)
        }

        if (spans.isNotEmpty() && searchPosition < spans.size) {
            val result = spans[searchPosition]
            val start = content.getSpanStart(result)
            val end = content.getSpanEnd(result)
            Selection.setSelection(content, start, end)
            content.setSpan(
                SearchFocusSpan(searchSpanBgColor, searchSpanFgColor),
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun clearSearchResult() {
        val content = tv_text_content.text as Spannable
        Selection.removeSelection(content)
        content.getSpans<SearchSpan>()
            .forEach {
                content.removeSpan(it)
            }
    }

    override fun showSearchBar() {
        bottombar.setSearchState(true)
        scroll.setMarignOptionaly(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        bottombar.setSearchState(false)
        scroll.setMarignOptionaly(bottom = 0)
    }

    inner class ArticleBinding : Binding() {
        var isFocusedSearch: Boolean = false
        var searchQuery: String? = null
        private var isLike : Boolean by RenderProp(false) { btn_like.isChecked = it }
        private var isBookmark : Boolean by RenderProp(false) { btn_bookmark.isChecked = it }
        private var isShowMenu : Boolean by RenderProp(false) {
            btn_settings.isChecked = it
            if (it) {
                submenu.open()
            } else {
                submenu.close()
            }
        }
        private var title : String by RenderProp("loading") { toolbar.title = it }
        private var category : String by RenderProp("loading") { toolbar.subtitle = it }
        private var categoryIcon : Int by RenderProp(R.drawable.logo_placeholder) {
            toolbar.logo = getDrawable(it)
        }
        private var isBigText : Boolean by RenderProp(false) {
            if (it) {
                tv_text_content.textSize = 18f
                btn_text_up.isChecked = true
                btn_text_down.isChecked = false
            } else {
                tv_text_content.textSize = 14f
                btn_text_up.isChecked = false
                btn_text_down.isChecked = true
            }
        }

        private var isDarkMode : Boolean by RenderProp(value = false, needInit = false) {
            switch_mode.isChecked = it
            delegate.localNightMode =
                if (it) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        }

        private var isLoadingContent by ObserveProp(true)
        var isSearch: Boolean by ObserveProp(false) {
            if (it)
                showSearchBar()
            else
                hideSearchBar()
        }
        private var searchResults: List<Pair<Int, Int>> by ObserveProp(emptyList())
        private var searchPosition: Int by ObserveProp(0)
        private var content: String by ObserveProp("loading") {
            tv_text_content.setText(it, TextView.BufferType.SPANNABLE)
            tv_text_content.movementMethod = ScrollingMovementMethod()
            tv_text_content.isFocusable = true
            tv_text_content.isFocusableInTouchMode = true
        }

        override fun onFinishInflate() {
            dependsOn<Boolean, Boolean, List<Pair<Int, Int>>, Int>(
                ::isLoadingContent,
                ::isSearch,
                ::searchResults,
                ::searchPosition
            ) {
                ilc, iss, sr, sp ->
                if (!ilc) {
                    if (iss) {
                        renderSearchResult(sr)
                        renderSearchPosition(sp)
                    } else {
                        clearSearchResult()
                    }
                }
                bottombar.bindSearchInfo(sr.size, sp)
            }

        }

        override fun bind(data: IViewModelState) {
            data as ArticleState
            isLike = data.isLike
            isBookmark = data.isBookmark
            isShowMenu = data.isShowMenu
            isBigText = data.isBigText
            isDarkMode = data.isDarkMode

            data.title?.let { title = it }
            data.category?.let { category = it }
            data.categoryIcon?.let { categoryIcon = it as Int }
            data.content.takeIf { it.isNotEmpty() } ?.let { content = it.first() as String }

            isLoadingContent = data.isLoadingContent
            isSearch = data.isSearch
            searchQuery = data.searchQuery
            searchResults = data.searchResult
            searchPosition = data.searchPosition
        }
        override fun saveUi(outState: Bundle) {
            outState.putBoolean(::isFocusedSearch.name, search_view?.hasFocus() ?: false)
        }
        override fun restoreUi(savedState: Bundle) {
            isFocusedSearch = savedState.getBoolean(::isFocusedSearch.name)
//            if (isFocusedSearch)
//                search_view.requestFocus()
        }

    }

}