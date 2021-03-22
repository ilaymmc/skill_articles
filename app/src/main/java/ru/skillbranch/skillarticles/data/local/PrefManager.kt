package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings

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

    private val settings : MutableLiveData<AppSettings> = MutableLiveData(AppSettings()).apply {
        observeForever {
            isDarkMode = it.isDarkMode
            isBigText = it.isBigText
        }
    }
    private val isAuthorized : MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        reloadAll()
    }


    //    var storedBoolean by PrefDelegate(false)
//    var storedString by PrefDelegate("")
//    var storedInt by PrefDelegate(Int.MAX_VALUE)
//
    fun clearAll() {
        preferences.edit().clear().apply()
        reloadAll()
    }

    private fun reloadAll() {
        settings.value = AppSettings(
            isDarkMode = isDarkMode,
            isBigText = isBigText
        )
    }

    fun getAppSettings(): LiveData<AppSettings> = settings

    fun setAppSettings(newSettings: AppSettings) {
        settings.value = newSettings
    }
    fun isAuth(): MutableLiveData<Boolean>  = isAuthorized
    fun setAuth(auth: Boolean): Unit {
        isAuthorized.value = auth
    }
}