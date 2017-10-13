package com.gh0u1l5.wechatmagician.frontend

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_WORLD_READABLE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.os.Bundle
import android.preference.PreferenceFragment

class PrefFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private val ARG_PREF_RES = "preferencesResId"

        fun newInstance(preferencesResId: Int): PrefFragment {
            val fragment = PrefFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PREF_RES, preferencesResId)
            }
            return fragment
        }
    }

    @SuppressLint("WorldReadableFiles")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesMode = MODE_WORLD_READABLE
        if (arguments != null) {
            val preferencesResId = arguments.getInt(ARG_PREF_RES)
            addPreferencesFromResource(preferencesResId)
            preferenceManager.sharedPreferences
                    .registerOnSharedPreferenceChangeListener(this)
        }
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (key == "settings_interface_hide_icon") {
            // Hide/Show the icon as required.
            val hide = preferences.getBoolean(key, false)
            val newState = if (hide) COMPONENT_ENABLED_STATE_DISABLED else COMPONENT_ENABLED_STATE_ENABLED
            val pkg = activity.packageName
            val componentName = ComponentName(pkg, "$pkg.frontend.MainActivityAlias")
            activity.packageManager.setComponentEnabledSetting(componentName, newState, DONT_KILL_APP)
        }
    }

}