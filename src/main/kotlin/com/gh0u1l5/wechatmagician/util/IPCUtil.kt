package com.gh0u1l5.wechatmagician.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

object IPCUtil {
    fun Intent.putExtra(name: String, value: Any): Intent {
        return when (value) {
            is Boolean      -> putExtra(name, value)
            is BooleanArray -> putExtra(name, value)
            is Bundle       -> putExtra(name, value)
            is Byte         -> putExtra(name, value)
            is ByteArray    -> putExtra(name, value)
            is Char         -> putExtra(name, value)
            is CharArray    -> putExtra(name, value)
            is CharSequence -> putExtra(name, value)
            is Double       -> putExtra(name, value)
            is DoubleArray  -> putExtra(name, value)
            is Float        -> putExtra(name ,value)
            is FloatArray   -> putExtra(name, value)
            is Int          -> putExtra(name, value)
            is IntArray     -> putExtra(name, value)
            is Long         -> putExtra(name, value)
            is LongArray    -> putExtra(name, value)
            is Short        -> putExtra(name, value)
            is ShortArray   -> putExtra(name, value)
            is String       -> putExtra(name, value)
            is Parcelable   -> putExtra(name, value)
            is Serializable -> putExtra(name, value)
            else -> throw Error("Intent.putExtra(): Unknown type: ${value::class.java}")
        }
    }

    fun Context.getProtectedSharedPreferences(name: String, mode: Int): SharedPreferences {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            getSharedPreferences(name, mode)
        } else {
            createDeviceProtectedStorageContext().getSharedPreferences(name, mode)
        }
    }
}