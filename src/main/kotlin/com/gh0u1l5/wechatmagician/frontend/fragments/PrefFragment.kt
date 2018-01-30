package com.gh0u1l5.wechatmagician.frontend.fragments

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.*
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.ACTION_UPDATE_PREF
import com.gh0u1l5.wechatmagician.Global.FOLDER_SHARED_PREFS
import com.gh0u1l5.wechatmagician.Global.LOG_TAG
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_BASE_DIR
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.SETTINGS_INTERFACE_HIDE_ICON
import com.gh0u1l5.wechatmagician.Global.SETTINGS_MODULE_LANGUAGE
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.util.FileUtil
import com.gh0u1l5.wechatmagician.util.LocaleUtil
import java.io.File

class PrefFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Move old shared preferences to device protected storage if it exists
            val oldPrefDir = "${context?.applicationInfo?.dataDir}/$FOLDER_SHARED_PREFS"
            val newPrefDir = "${context?.applicationInfo?.deviceProtectedDataDir}/$FOLDER_SHARED_PREFS"
            try {
                File(oldPrefDir).renameTo(File(newPrefDir))
            } catch (t: Throwable) {
                // Ignore this one
            }
            preferenceManager.setStorageDeviceProtected()
        }

        if (arguments != null) {
            val preferencesResId = arguments!!.getInt(ARG_PREF_RES)
            val preferencesName = arguments!!.getString(ARG_PREF_NAME)
            preferenceManager.sharedPreferencesName = preferencesName
            setPreferencesFromResource(preferencesResId, rootKey)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context ?: return null
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            setBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
        }
    }

    override fun onStart() {
        super.onStart()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    // Reference: https://github.com/rovo89/XposedBridge/issues/206
    override fun onPause() {
        // Set shared preferences as world readable.
        val folder = File("$MAGICIAN_BASE_DIR/$FOLDER_SHARED_PREFS")
        val file = File(folder, preferenceManager.sharedPreferencesName + ".xml")
        FileUtil.setWorldReadable(file)

        // Notify the backend to reload the preferences
        context?.sendBroadcast(Intent(ACTION_UPDATE_PREF))

        super.onPause()
    }

    override fun onStop() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (key == SETTINGS_INTERFACE_HIDE_ICON) {
            // Hide/Show the icon as required.
            try {
                val hide = preferences.getBoolean(SETTINGS_INTERFACE_HIDE_ICON, false)
                val newState = if (hide) COMPONENT_ENABLED_STATE_DISABLED else COMPONENT_ENABLED_STATE_ENABLED
                val className = "$MAGICIAN_PACKAGE_NAME.frontend.MainActivityAlias"
                val componentName = ComponentName(MAGICIAN_PACKAGE_NAME, className)
                context!!.packageManager.setComponentEnabledSetting(componentName, newState, DONT_KILL_APP)
            } catch (t: Throwable) {
                Log.e(LOG_TAG, "Cannot hide icon: $t")
                Toast.makeText(context, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        if (key == SETTINGS_MODULE_LANGUAGE) {
            try {
                val language = LocaleUtil.getLanguage(activity!!)
                LocaleUtil.setLocale(activity!!, language)
                activity!!.recreate()
            } catch (t: Throwable) {
                Log.e(LOG_TAG, "Cannot change language: $t")
                Toast.makeText(context, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val ARG_PREF_RES = "preferencesResId"
        private const val ARG_PREF_NAME = "preferencesFileName"

        fun newInstance(preferencesResId: Int, preferencesName: String): PrefFragment {
            val fragment = PrefFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PREF_RES, preferencesResId)
                putString(ARG_PREF_NAME, preferencesName)
            }
            return fragment
        }
    }
}