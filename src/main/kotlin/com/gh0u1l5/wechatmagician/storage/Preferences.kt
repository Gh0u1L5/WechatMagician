package com.gh0u1l5.wechatmagician.storage

import android.os.FileObserver
import com.gh0u1l5.wechatmagician.util.FileUtil
import de.robv.android.xposed.XposedBridge.log
import java.io.File
import java.io.FileNotFoundException

@Suppress("UNCHECKED_CAST")
class Preferences {

    // NOTE: Only one instance of FileObserver will get notified.
    //       https://issuetracker.google.com/issues/37017033
    companion object {
        private lateinit var PREF_PATH: String
        private lateinit var observer: FileObserver
        val entries: MutableMap<String, Preferences> = mutableMapOf()

        // NOTE: This should only be called once, before any initialization happens.
        fun setPreferenceFolder(folder: String) {
            PREF_PATH = folder
            File(folder).mkdirs()
            observer = object : FileObserver(folder, CREATE or MODIFY or CLOSE_WRITE) {
                override fun onEvent(event: Int, filename: String) {
                    if (filename !in entries) {
                        return
                    }
                    entries[filename]?.loadFile("$folder/$filename")
                }
            }
            observer.startWatching()
        }
    }

    @Volatile private var preferences: HashMap<String, Any?>? = null

    fun load(filename: String) {
        loadFile("$PREF_PATH/$filename")
        entries[filename] = this
    }

    private fun loadFile(path: String) {
        try {
            preferences = FileUtil.readObjectFromDisk(path) as HashMap<String, Any?>
        } catch (_: FileNotFoundException) {
            // Ignore this one
        } catch (e: Throwable) {
            log("PREF => ${e.message}")
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