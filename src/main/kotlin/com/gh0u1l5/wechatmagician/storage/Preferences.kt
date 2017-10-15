package com.gh0u1l5.wechatmagician.storage

import com.gh0u1l5.wechatmagician.util.FileUtil
import de.robv.android.xposed.XposedBridge.log
import java.io.File
import java.io.FileNotFoundException
import kotlin.concurrent.timer

@Suppress("UNCHECKED_CAST")
class Preferences {

    @Volatile private var preferences: HashMap<String, Any?>? = null
    @Volatile private var preferencesPath: String = ""
    @Volatile private var preferencesLastModified: Long = 0L

    fun load(path: String, watchUpdate: Boolean = true) {
        // NOTE: FileObserver returns 32768 in some devices,
        //       so we have to watch the file manually.
        preferencesPath = path
        try {
            preferences = FileUtil.readObjectFromDisk(path) as HashMap<String, Any?>
            preferencesLastModified = File(path).lastModified()
        } catch (_: FileNotFoundException) {
            // Ignore this one
        } catch (e: Throwable) {
            log("PREF => ${e.message}")
            return
        }
        if (watchUpdate) {
            timer(period = 1000, action = { reload() })
        }
    }

    private fun reload() {
        // NOTE: reload will check if the file has been modified,
        //       so it shouldn't cause too much overhead.
        if (!isModified()) {
            return
        }
        load(preferencesPath, false) // prevent creating infinite threads
    }

    private fun isModified(): Boolean {
        val file = File(preferencesPath)
        try {
            if (!file.exists()) {
                return false
            }
            return file.lastModified() != preferencesLastModified
        } catch (e: Throwable) {
            log("PREF => ${e.message}")
            return false
        }
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences?.get(key) as? Boolean ?: defValue
    }

    fun getString(key: String, defValue: String = ""): String {
        return preferences?.get(key) as? String ?: defValue
    }

    fun getStringList(key: String, defValue: List<String> = listOf()): List<String> {
        return preferences?.get(key) as? List<String> ?: defValue
    }
}