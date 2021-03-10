package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate

// Реализуй в классе PrefManager(context:Context) (ru.skillbranch.skillarticles.data.local.PrefManager)
// свойство val preferences : SharedPreferences проинициализированое экземпляром SharedPreferences приложения.
// И метод fun clearAll() - очищающий все сохраненные значения SharedPreferences приложения.
// Использовать PrefManager из androidx (import androidx.preference.PreferenceManager

class PrefManager(context: Context) {

    internal val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

//    var isDarkMode by PrefDelegate(false)

    var storedBoolean by PrefDelegate(false)
    var storedString by PrefDelegate("")
    var storedInt by PrefDelegate(Int.MAX_VALUE)

    fun clearAll() {
        preferences.edit().clear().apply()
    }
}