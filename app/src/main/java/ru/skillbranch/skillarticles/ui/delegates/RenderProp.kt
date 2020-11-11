package ru.skillbranch.skillarticles.ui.delegates

import ru.skillbranch.skillarticles.ui.base.Binding
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RenderProp<T : Any>(
    var value: T,
    private val needInit: Boolean = true,
    private val onChange: ((T) -> Unit)? = null
) : ReadWriteProperty<Binding, T> {

    private val listeners = mutableListOf<()->Unit>()

    fun bind() {
        if (needInit)
            onChange?.invoke(value)
    }

    operator fun provideDelegate(
        thisRef: Binding,
        prop: KProperty<*>
    ) : ReadWriteProperty<Binding, T> {
        val delegate = RenderProp(value, true, onChange)
        registerDelegate(thisRef, prop.name, delegate)
        return delegate
    }

    override fun getValue(thisRef: Binding, property: KProperty<*>): T = value

    override fun setValue(thisRef: Binding, property: KProperty<*>, newValue: T) {
        if (newValue != value) {
            value = newValue
            onChange?.invoke(value)
            listeners.forEach {
                it.invoke()
            }
        }
    }

    fun addListener(listener: ()->Unit) {
        listeners.add(listener)
    }

    private fun registerDelegate(thisRef: Binding, name: String, delegate: RenderProp<T>) {
        thisRef.delegates[name] = delegate
    }
}
