package ru.skillbranch.skillarticles.data.delegates

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.squareup.moshi.JsonAdapter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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

internal class SharedPreferencesLiveDataObj<T>(
    var sharedPrefs: SharedPreferences,
    var key: String,
    private val adapter: JsonAdapter<T>
) : LiveData<T?>() {

    private val preferencesChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, shKey ->
            if (shKey == key) {
                value = sharedPrefs.getString(key, null)?.let { adapter.fromJson(it) }
            }
        }

    override fun onActive() {
        super.onActive()
        value = sharedPrefs.getString(key, null)?.let { adapter.fromJson(it) }
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
        super.onInactive()
    }
}

class PrefLiveObjDelegate<T>(
    private val fieldKey: String,
    private val adapter: JsonAdapter<T>,
    private val preferences: SharedPreferences
) : ReadOnlyProperty<Any?, LiveData<T?>> {
    private var storedValue: LiveData<T?>? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): LiveData<T?> {
        if (storedValue == null) {
            storedValue = SharedPreferencesLiveDataObj(preferences, fieldKey, adapter)
        }
        return storedValue!!
    }
}
