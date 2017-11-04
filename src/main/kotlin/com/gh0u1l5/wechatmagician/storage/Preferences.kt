package com.gh0u1l5.wechatmagician.storage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.gh0u1l5.wechatmagician.Global.ACTION_UPDATE_PREF
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_STRING_LIST_KEYS
import com.gh0u1l5.wechatmagician.Global.WECHAT_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.util.FileUtil.getApplicationDataDir
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge.log
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class Preferences {

    private val listCacheLock = ReentrantReadWriteLock()
    private val listCache = mutableMapOf<String, List<String>>()

    private var content: XSharedPreferences? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            content?.reload()
            cacheStringList()
        }
    }

    // load registers the receiver and loads the specific shared preferences.
    fun load(context: Context?, preferencesName: String) {
        try {
            var dataDir = getApplicationDataDir(context)
            dataDir = dataDir?.replace(WECHAT_PACKAGE_NAME, MAGICIAN_PACKAGE_NAME)
            content = XSharedPreferences(File("$dataDir/shared_prefs/$preferencesName.xml"))
            context?.registerReceiver(receiver, IntentFilter(ACTION_UPDATE_PREF))
        } catch (_: FileNotFoundException) {
            // Ignore this one
        } catch (e: Throwable) {
            log("PREF => $e")
        }
    }

    fun cacheStringList() {
        PREFERENCE_STRING_LIST_KEYS.forEach { key ->
            val list = content?.getString(key, "")?.split(" ")
            listCacheLock.write {
                listCache[key] = list ?: return
            }
        }
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean =
            content?.getBoolean(key, defValue) ?: defValue

    fun getString(key: String, defValue: String = ""): String =
            content?.getString(key, defValue) ?: defValue

    fun getStringList(key: String, defValue: List<String> = listOf()): List<String> {
        listCacheLock.read {
            return listCache[key] ?: defValue
        }
    }
}
