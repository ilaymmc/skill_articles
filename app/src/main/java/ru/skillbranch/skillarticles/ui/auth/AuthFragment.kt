package ru.skillbranch.skillarticles.ui.auth

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Spannable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.set
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.NavAction
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_auth.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.custom.spans.UnderlineSpan
import ru.skillbranch.skillarticles.viewmodels.auth.AuthViewModel
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

// 02:03:37

class AuthFragment : BaseFragment<AuthViewModel>() {
    override val viewModel: AuthViewModel by viewModels()

    override val layout: Int = R.layout.fragment_auth
    private val args : AuthFragmentArgs by navArgs()

    override fun setupViews() {

        tv_privacy.setOnClickListener {
            viewModel.navigate(NavigationCommand.To(R.id.page_privacy_policy))
        }

        bt_login.setOnClickListener {
            viewModel.handleLogin(
                et_login.text.toString(),
                et_password.text.toString(),
                args.privateDestination.takeIf { it != -1 }
            )
        }

        val color = root.attrValue(R.attr.colorPrimary)
        (tv_access_code.text as Spannable).let { it[0..it.length] = UnderlineSpan(color) }
        (tv_privacy.text as Spannable).let { it[0..it.length] = UnderlineSpan(color) }
    }

}