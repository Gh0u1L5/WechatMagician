package com.gh0u1l5.wechatmagician.storage

import de.robv.android.xposed.XSharedPreferences
import kotlin.concurrent.thread
import kotlin.concurrent.timer

class Preferences(private val lists: List<String> = listOf()) {

    @Volatile private var preferences: XSharedPreferences? = null
    private val listCache: MutableMap<String, List<String>> = mutableMapOf()

    fun load(preferences: XSharedPreferences?, watchUpdate: Boolean = true) {
        // NOTE: FileObserver returns 32768 in some devices,
        //       so we have to watch the file manually.
        this.preferences = preferences
        thread(start = true) {
            lists.forEach { cacheList(it) }
        }
        if (watchUpdate) {
            timer(period = 1000, action = { reload() })
        }
    }

    private fun reload() {
        // NOTE: reload will check if the file has been modified,
        //       so it shouldn't cause too much overhead.
        if (this.preferences?.hasFileChanged() == true) {
            this.preferences?.reload()
            lists.forEach { cacheList(it) }
        }
    }

    private fun cacheList(key: String, delimiter: String = " ") {
        val list = getString(key, "").split(delimiter)
        synchronized(listCache) {
            listCache[key] = list
        }
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences?.getBoolean(key, defValue) ?: defValue
    }

    fun getString(key: String, defValue: String = ""): String {
        return preferences?.getString(key, defValue) ?: defValue
    }

    fun getStringList(key: String, defValue: List<String> = listOf()): List<String> {
        synchronized(listCache) {
            return listCache[key] ?: defValue
        }
    }
}