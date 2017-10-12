package com.gh0u1l5.wechatmagician.frontend

import android.annotation.SuppressLint
import android.content.Context.MODE_WORLD_READABLE
import android.os.Bundle
import android.preference.PreferenceFragment

class PrefFragment : PreferenceFragment() {

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
        }
    }
}