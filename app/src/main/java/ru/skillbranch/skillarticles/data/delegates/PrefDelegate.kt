package ru.skillbranch.skillarticles.data.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager
import java.lang.IllegalArgumentException
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//    Реализуй делегат PrefDelegate<T>(private val defaultValue: T) :
//    ReadWriteProperty<PrefManager, T?> (ru.skillbranch.skillarticles.data.delegates.PrefDelegate)
//    возвращающий значений примитивов (Boolean, String, Float, Int, Long)
//
//    Пример: var storedBoolean by PrefDelegate(false)
//    var storedString by PrefDelegate("")
//    var storedFloat by PrefDelegate(0f)
//    var storedInt by PrefDelegate(0)
//    var storedLong by PrefDelegate(0)
//
//    Реализуй в классе PrefManager(context:Context) (ru.skillbranch.skillarticles.data.local.PrefManager)
//    свойство val preferences : SharedPreferences проинициализированое экземпляром SharedPreferences приложения.
//    И метод fun clearAll() - очищающий все сохраненные значения SharedPreferences приложения.
//    Использовать PrefManager из androidx (import androidx.preference.PreferenceManager)


class PrefDelegate<T>(private val defaultValue: T) {
    private var storedValue : T? = null

    operator fun provideDelegate(
        thisRef: PrefManager,
        property: KProperty<*>
    ) : ReadWriteProperty<PrefManager, T>  {
        val key = property.name
        return object : ReadWriteProperty<PrefManager, T> {
            override fun getValue(thisRef: PrefManager, property: KProperty<*>): T {
                with(thisRef.preferences) {
                    return when (defaultValue) {
                        is Boolean -> getBoolean(key, defaultValue)
                        is String -> getString(key, defaultValue)
                        is Float -> getFloat(key, defaultValue)
                        is Int -> getInt(key, defaultValue)
                        is Long -> getLong(key, defaultValue)
                        else ->
                            error("Unsupported type of ${property.name}")
                    } as T
                }
            }
            override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T) {
                thisRef.preferences.edit(commit = true) {
                    when (value) {
                        is Boolean ->
                            putBoolean(key, value)
                        is String ->
                            putString(key, value)
                        is Float ->
                            putFloat(key, value)
                        is Int ->
                            putInt(key, value)
                        is Long ->
                            putLong(key, value)
                        else ->
                            throw IllegalArgumentException("Unsupported type of ${property.name}")
                    } as T
                }
            }
        }

    }
}

class PrefLiveDelegate<T>(
    private val fieldKey: String,
    private val defaultValue: T,
    private val preferences: SharedPreferences
    ) : ReadOnlyProperty<Any?, LiveData<T>> {
    private var storedValue: LiveData<T>? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): LiveData<T> {
        if (storedValue == null)
            storedValue = SharedPreferencesLiveData(preferences, fieldKey, defaultValue)
        return storedValue!!
    }
}

internal class SharedPreferencesLiveData<T>(
    var sharedPrefs: SharedPreferences,
    var key: String,
    var defValue: T
) : LiveData<T>() {

    private val preferencesChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, shKey ->
            if (shKey == key) {
                value = readValue(defValue)
            }
        }

    override fun onActive() {
        super.onActive()
        value = readValue(defValue)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
        super.onInactive()
    }

    private fun readValue(defaultValue: T): T {
        return when(defaultValue) {
            is Int -> sharedPrefs.getInt(key, defaultValue) as T
            is Long -> sharedPrefs.getLong(key, defaultValue) as T
            is Float -> sharedPrefs.getFloat(key, defaultValue) as T
            is String -> sharedPrefs.getString(key, defaultValue) as T
            is Boolean -> sharedPrefs.getBoolean(key, defaultValue) as T
            else -> error("This type $defaultValue cannot be stored in shared preferences")
        }
    }

}


//class PrefDelegate<T>(private val defaultValue: T) : ReadWriteProperty<PrefManager, T?> {
//    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
//        with (thisRef.preferences) {
//            return when(defaultValue) {
//                is Boolean -> getBoolean(property.name, defaultValue)
//                is String -> getString(property.name, defaultValue)
//                is Float -> getFloat(property.name, defaultValue)
//                is Int -> getInt(property.name, defaultValue)
//                is Long -> getLong(property.name, defaultValue)
//                else ->
//                    error("Unsupported type of ${property.name}")
//            } as T
//        }
//    }
//
//    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
//        thisRef.preferences.edit(commit = true) {
//            when(value) {
//                is Boolean ->
//                    putBoolean(property.name, value)
//                is String ->
//                    putString(property.name, value)
//                is Float ->
//                    putFloat(property.name, value)
//                is Int ->
//                    putInt(property.name, value)
//                is Long ->
//                    putLong(property.name, value)
//                else ->
//                    throw IllegalArgumentException("Unsupported type of ${property.name}")
//            } as T
//        }
//    }
//}