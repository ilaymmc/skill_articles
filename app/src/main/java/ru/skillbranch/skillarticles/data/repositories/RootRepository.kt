package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.LoginReq
import ru.skillbranch.skillarticles.data.remote.req.RefreshReq

object RootRepository {
    private val preferences = PrefManager
    private val network = NetworkManager.api
    fun isAuth() : LiveData<Boolean> = preferences.isAuthLive

//    fun setAuth(auth: Boolean)  {
//        preferences.isAuth = auth
//    }

    suspend fun refresh(): String {
        val auth = network.refresh(RefreshReq(preferences.refreshToken))
        preferences.refreshToken = auth.refreshToken
        preferences.accessToken = "Bearer ${auth.accessToken}"
        return preferences.accessToken
    }

    suspend fun login(login: String, pass: String) {
        val auth = network.login(LoginReq(login, pass))
        preferences.profile = auth.user
        preferences.accessToken = "Bearer ${auth.accessToken}"
        preferences.refreshToken = auth.refreshToken
    }
}