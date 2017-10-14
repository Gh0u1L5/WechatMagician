package com.gh0u1l5.wechatmagician.frontend.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager.*
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gh0u1l5.wechatmagician.R
import java.io.File

class PrefFragment : PreferenceFragment() {

    class OnSharedPreferenceChangeListener(private val context: Context): SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
            if (key == "settings_interface_hide_icon") {
                // Hide/Show the icon as required.
                val hide = preferences.getBoolean(key, false)
                val newState = if (hide) COMPONENT_ENABLED_STATE_DISABLED else COMPONENT_ENABLED_STATE_ENABLED
                val pkg = context.packageName
                val componentName = ComponentName(pkg, "$pkg.frontend.MainActivityAlias")
                context.packageManager.setComponentEnabledSetting(
                        componentName, newState, DONT_KILL_APP
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            val preferencesResId = arguments.getInt(ARG_PREF_RES)
            addPreferencesFromResource(preferencesResId)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(
                    OnSharedPreferenceChangeListener(activity.applicationContext)
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            setBackgroundColor(getColor(R.color.card_background))
        }
    }

    // NOTE: On Android N, the support of MODE_WORLD_READABLE has been removed.
    //       So whenever the user exits from the PrefFragment we set file permissions manually.
    @SuppressLint("SetWorldReadable")
    override fun onPause() {
        super.onPause()
        val prefDir = "${activity.applicationInfo.dataDir}/shared_prefs/"
        val filename = "${preferenceManager.sharedPreferencesName}.xml"
        File("$prefDir$filename").setReadable(true, false)
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

        fun newInstance(preferencesResId: Int): PrefFragment {
            val fragment = PrefFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PREF_RES, preferencesResId)
            }
            return fragment
        }
    }
}