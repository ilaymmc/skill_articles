package ru.skillbranch.skillarticles.data.remote.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.local.PrefManager.accessToken
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.RestService
import ru.skillbranch.skillarticles.data.remote.req.RefreshReq
import ru.skillbranch.skillarticles.data.repositories.RootRepository


class TokenAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == 401) {
            return runBlocking {
                val newAccessToken = RootRepository.refresh()
                response.request.newBuilder().header("Authorization", newAccessToken).build()
            }
        }
        return response.request.newBuilder().build()
    }
}