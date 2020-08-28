package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate

// Реализуй в классе PrefManager(context:Context) (ru.skillbranch.skillarticles.data.local.PrefManager)
// свойство val preferences : SharedPreferences проинициализированое экземпляром SharedPreferences приложения.
// И метод fun clearAll() - очищающий все сохраненные значения SharedPreferences приложения.
// Использовать PrefManager из androidx (import androidx.preference.PreferenceManager

class PrefManager(context: Context) {

    var isDarkMode: Boolean by PrefDelegate(false)

    val preferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    fun clearAll() {
        preferences.edit (commit = true) {
            clear()
        }
    }
}