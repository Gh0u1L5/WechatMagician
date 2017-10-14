package com.gh0u1l5.wechatmagician.storage

import de.robv.android.xposed.XSharedPreferences
import kotlin.concurrent.timer

class Preferences {

    @Volatile private var preferences: XSharedPreferences? = null

    fun load(preferences: XSharedPreferences?) {
        // NOTE: FileObserver returns 32768 in some devices,
        //       so we have to watch the file manually.
        this.preferences = preferences
        timer(period = 1000, action = { reload() })
    }

    private fun reload() {
        // NOTE: reload will check if the file has been modified,
        //       so it shouldn't cause too much overhead.
        this.preferences?.reload()
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences?.getBoolean(key, defValue) ?: defValue
    }

    fun getString(key: String, defValue: String): String {
        return preferences?.getString(key, defValue) ?: defValue
    }
}