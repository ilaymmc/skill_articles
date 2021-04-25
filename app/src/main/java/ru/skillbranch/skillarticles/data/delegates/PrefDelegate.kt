package ru.skillbranch.skillarticles.data.delegates

import androidx.core.content.edit
import com.squareup.moshi.JsonAdapter
import ru.skillbranch.skillarticles.data.local.PrefManager
import java.lang.IllegalArgumentException
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

class PrefObjDelegate<T>(
    private val adapter: JsonAdapter<T>
) {
    private var storedValue : T? = null

    operator fun provideDelegate(
        thisRef: PrefManager,
        property: KProperty<*>
    ) : ReadWriteProperty<PrefManager, T?>  {
        val key = property.name
        return object : ReadWriteProperty<PrefManager, T?> {
            override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
                if (storedValue == null) {
                    storedValue = thisRef.preferences.getString(key, null)
                        ?.let { adapter.fromJson(it) }

                }
                return storedValue
            }

            override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
                storedValue = value
                with(thisRef.preferences.edit()) {
                    putString(key, value?.let { adapter.toJson(it) })
                    apply()
                }
            }
        }
    }
}
