package com.vdotok.app.base

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference
import java.lang.reflect.Type
import kotlin.reflect.KProperty


/**
 * Created By: VdoTok
 * Date & Time: On 17/11/2021 At 11:56 AM in 2021
 */
abstract class BasePreferences {
    companion object {
        private var context: WeakReference<Context>? = null

        /**
         * Initialize PrefDelegate with a Context reference.
         *
         * **This method needs to be called before any other usage of PrefDelegate!!**
         */
        fun init(context: Context) {
            Companion.context = WeakReference(context)
        }
    }

    private val prefs: SharedPreferences by lazy {
        val co: Context = context!!.get()
            ?: throw IllegalStateException(
                "Context was not initialized." +
                        " Call Preferences.init(context) before using it"
            )
        co.getSharedPreferences(javaClass.simpleName, Context.MODE_PRIVATE)
    }

    private val listeners = mutableListOf<SharedPrefsListener>()

    interface SharedPrefsListener {
        fun onSharedPrefChanged(property: KProperty<*>)
    }

    fun addListener(sharedPrefsListener: SharedPrefsListener) {
        listeners.add(sharedPrefsListener)
    }

    fun removeListener(sharedPrefsListener: SharedPrefsListener) {
        listeners.remove(sharedPrefsListener)
    }

    fun clearListeners() = listeners.clear()
    abstract class PrefDelegate<T>(val prefKey: String?) {
        abstract operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        abstract operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)

    }

    abstract class PrefDelegateObject<T>(val prefKey: String?) {
        abstract operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        abstract operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)

    }

    fun objectPref(classType: Type, prefKey: String? = null, defaultValue: String? = null) =
        ObjectPrefDelegate(classType, prefKey, defaultValue)

    inner class ObjectPrefDelegate(
        val classType: Type,
        prefKey: String? = null,
        val defaultValue: String?
    ) :
        PrefDelegateObject<Any?>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Any? {
            val jsonValue = prefs.getString(prefKey ?: property.name, defaultValue)
            return getObject(jsonValue, defaultValue, classType)
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Any?) {
            prefs.edit().putString(prefKey ?: property.name, Gson().toJson(value, classType))
                .apply()
            onPrefChanged(property)
        }

    }

    fun arrayListPref(
        classType: ArrayList<Any>,
        prefKey: String? = null,
        defaultValue: String? = null,
        arrayListDataType: Type
    ) =
        ArraylistDelegate(classType, prefKey, defaultValue, arrayListDataType)

    inner class ArraylistDelegate(
        val list: ArrayList<Any>,
        prefKey: String? = null,
        val defaultValue: String?,
        val arrayListDataType: Type
    ) :
        PrefDelegateObject<Any?>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Any? {
            val jsonValue = prefs.getString(prefKey ?: property.name, defaultValue)
//            val type: Type = object : TypeToken<List<arrayListDataType>>() {}.type
            val type = TypeToken.getParameterized(ArrayList::class.java, arrayListDataType).type
            return getObject(jsonValue, defaultValue, type)
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Any?) {
            prefs.edit().putString(prefKey ?: property.name, Gson().toJson(value)).apply()
            onPrefChanged(property)
        }

    }


    fun stringPref(prefKey: String? = null, defaultValue: String? = null) =
        StringPrefDelegate(prefKey, defaultValue)


    inner class StringPrefDelegate(prefKey: String? = null, val defaultValue: String?) :
        PrefDelegate<String?>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String? =
            prefs.getString(prefKey ?: property.name, defaultValue)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            prefs.edit().putString(prefKey ?: property.name, value).apply()
            onPrefChanged(property)
        }

    }

    fun intPref(prefKey: String? = null, defaultValue: Int = 0) =
        IntPrefDelegate(prefKey, defaultValue)

    inner class IntPrefDelegate(prefKey: String? = null, val defaultValue: Int) :
        PrefDelegate<Int>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = prefs.getInt(
            prefKey
                ?: property.name, defaultValue
        )

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            prefs.edit().putInt(prefKey ?: property.name, value).apply()
            onPrefChanged(property)
        }
    }


    fun floatPref(prefKey: String? = null, defaultValue: Float = 0f) =
        FloatPrefDelegate(prefKey, defaultValue)

    inner class FloatPrefDelegate(prefKey: String? = null, val defaultValue: Float) :
        PrefDelegate<Float>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = prefs.getFloat(
            prefKey
                ?: property.name, defaultValue
        )

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
            prefs.edit().putFloat(prefKey ?: property.name, value).apply()
            onPrefChanged(property)
        }
    }


    fun booleanPref(prefKey: String? = null, defaultValue: Boolean = false) =
        BooleanPrefDelegate(prefKey, defaultValue)

    inner class BooleanPrefDelegate(prefKey: String? = null, val defaultValue: Boolean) :
        PrefDelegate<Boolean>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = prefs.getBoolean(
            prefKey
                ?: property.name, defaultValue
        )

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            prefs.edit().putBoolean(prefKey ?: property.name, value).apply()
            onPrefChanged(property)
        }
    }


    fun longPref(prefKey: String? = null, defaultValue: Long = 0L) =
        LongPrefDelegate(prefKey, defaultValue)

    inner class LongPrefDelegate(prefKey: String? = null, val defaultValue: Long) :
        PrefDelegate<Long>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = prefs.getLong(
            prefKey
                ?: property.name, defaultValue
        )

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
            prefs.edit().putLong(prefKey ?: property.name, value).apply()
            onPrefChanged(property)
        }
    }


    fun stringSetPref(prefKey: String? = null, defaultValue: Set<String> = HashSet<String>()) =
        StringSetPrefDelegate(prefKey, defaultValue)

    inner class StringSetPrefDelegate(prefKey: String? = null, val defaultValue: Set<String>) :
        PrefDelegate<Set<String>>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Set<String> =
            prefs.getStringSet(
                prefKey
                    ?: property.name, defaultValue
            )!!

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Set<String>) {
            prefs.edit().putStringSet(prefKey ?: property.name, value).apply()
            onPrefChanged(property)
        }
    }

    private fun onPrefChanged(property: KProperty<*>) {
        listeners.forEach { it.onSharedPrefChanged(property) }
    }

    /**
     * Function to clear all prefs from storage
     * */
    fun clearApplicationPrefs() {
        prefs.edit(true) {
            this.clear().apply()
        }
    }

    /**
     * finds value on given key.
     * [T] is the type of value
     * @param defaultValue optional default value - will take null for strings, false for bool and -1 for numeric values if [defaultValue] is not specified
     */
    inline fun <reified T : Any> getObject(
        jsonValue: String?,
        defaultValue: T? = null,
        classType: Type
    ): T? {
        return if (!jsonValue.isNullOrEmpty()) {
            Gson().fromJson<T>(jsonValue, classType)
        } else {
            defaultValue
        }
    }
}