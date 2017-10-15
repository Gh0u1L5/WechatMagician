package com.gh0u1l5.wechatmagician.frontend.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.util.FileUtil
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

@Suppress("UNCHECKED_CAST")
class PrefFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    // NOTE: On Android N, we no longer have the permission to read other applications' preferences.
    //       So whenever the user exits from the PrefFragment we write our serialized cache to the
    //       external storage and set the file permissions manually.
    private val cacheLock = ReentrantLock()
    private var cache: HashMap<String, Any?> = hashMapOf()
    private var listKeys: HashMap<String, Boolean> = hashMapOf()

    private val writingThreads: MutableList<Thread> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load the preferences
        val manager = preferenceManager
        if (arguments != null) {
            val preferencesResId = arguments.getInt(ARG_PREF_RES)
            manager.sharedPreferencesName = arguments.getString(ARG_PREF_NAME)
            addPreferencesFromResource(preferencesResId)
            listKeys = arguments.getSerializable(ARG_PREF_LIST_KEYS) as HashMap<String, Boolean>
        }
        // Start new thread to cache the preferences
        writingThreads.add(thread(start = true) {
            cacheLock.withLock {
                for (pair in manager.sharedPreferences.all) {
                    addCache(pair.key, pair.value)
                }
            }
        })
        manager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            setBackgroundColor(getColor(R.color.card_background))
        }
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (key == "settings_interface_hide_icon") {
            // Hide/Show the icon as required.
            try {
                val hide = preferences.getBoolean(key, false)
                val newState = if (hide) COMPONENT_ENABLED_STATE_DISABLED else COMPONENT_ENABLED_STATE_ENABLED
                val pkg = activity.packageName
                val componentName = ComponentName(pkg, "$pkg.frontend.MainActivityAlias")
                activity.packageManager.setComponentEnabledSetting(componentName, newState, DONT_KILL_APP)
            } catch (e: Throwable) {
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }
            return // hide_icon is useless for Xposed part.
        }
        addCache(key, preferences.all[key])
    }

    @SuppressLint("SetWorldReadable")
    override fun onPause() {
        super.onPause()
        val storage = Environment.getExternalStorageDirectory().path + "/WechatMagician"
        val prefName = preferenceManager.sharedPreferencesName
        thread(start = true) {
            try {
                writingThreads.forEach { it.join() } // wait for all writing threads to finish.
                FileUtil.writeObjectToDisk("$storage/.prefs/$prefName", cache)
                File("$storage/.prefs/$prefName").setReadable(true, false)
            } catch (e: Throwable) {
                Log.e("WechatMagician", e.message)
            }
        }
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    private fun addCache(key: String, value: Any?) {
        if (key in listKeys) {
            if (value is String) {
                val list = value.split(" ")
                cacheLock.withLock { cache[key] = list }
                return
            }
            throw IllegalArgumentException("Unexpected type: $key should be string")
        }
        cacheLock.withLock { cache[key] = value }
    }

    private fun getColor(resId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(resId, context.theme)
        } else {
            resources.getColor(resId)
        }
    }

    companion object {
        private val ARG_PREF_RES = "preferencesResId"
        private val ARG_PREF_NAME = "preferencesFileName"
        private val ARG_PREF_LIST_KEYS = "preferencesStringListKeys"

        fun newInstance(
                preferencesResId: Int,
                preferencesName: String,
                preferencesListKeys: HashMap<String, Boolean> = hashMapOf()): PrefFragment {
            val fragment = PrefFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PREF_RES, preferencesResId)
                putString(ARG_PREF_NAME, preferencesName)
                putSerializable(ARG_PREF_LIST_KEYS, preferencesListKeys)
            }
            return fragment
        }
    }
}