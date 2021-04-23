package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.JsonConverter.moshi
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefLiveDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefLiveObjDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefObjDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings
import ru.skillbranch.skillarticles.data.models.User

// Реализуй в классе PrefManager(context:Context) (ru.skillbranch.skillarticles.data.local.PrefManager)
// свойство val preferences : SharedPreferences проинициализированое экземпляром SharedPreferences приложения.
// И метод fun clearAll() - очищающий все сохраненные значения SharedPreferences приложения.
// Использовать PrefManager из androidx (import androidx.preference.PreferenceManager

object PrefManager {

    internal val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    var isDarkMode by PrefDelegate(false)
    var isBigText by PrefDelegate(false)
    var isAuth by PrefDelegate(false)
    var profile: User? by PrefObjDelegate(moshi.adapter(User::class.java))

    var accessToken by PrefDelegate("")
    var refreshToken by PrefDelegate("")

    val isAuthLive: LiveData<Boolean> by lazy {
        val token by PrefLiveDelegate("accessToken", "", preferences)
        token.map { it.isEmpty() }
    }
    val profileLive: LiveData<User?> by PrefLiveObjDelegate("profile", moshi.adapter(User::class.java), preferences)

    val appSettings = MediatorLiveData<AppSettings>().apply {
        val isDarkModeLive: LiveData<Boolean> by PrefLiveDelegate("isDarkMode", false, preferences)
        val isBigTextLive: LiveData<Boolean> by PrefLiveDelegate("isBigText", false, preferences)
        value = AppSettings()

        addSource(isDarkModeLive) {
            value = value!!.copy(isDarkMode = it)
        }

        addSource(isBigTextLive) {
            value = value!!.copy(isBigText = it)
        }
    } .distinctUntilChanged()

    fun clearAll() {
        preferences.edit().clear().apply()
    }

//    fun getAppSettings(): LiveData<AppSettings> = settings

//    fun setAppSettings(newSettings: AppSettings) {
//        settings.value = newSettings
//    }
//    fun isAuth(): MutableLiveData<Boolean>  = isAuthorized
//    fun setAuth(auth: Boolean): Unit {
//        isAuthorized.value = auth
//    }
}