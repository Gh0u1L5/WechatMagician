package com.gh0u1l5.wechatmagician.frontend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import com.gh0u1l5.wechatmagician.frontend.fragments.PrefFragment.Companion.notifyPreferenceChange

class PreferenceProvider : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val keys = intent?.getStringArrayExtra("preference_keys") ?: return
        if (keys.isNotEmpty() && keys[0] == "*") {
            val preferenceName = intent.getStringExtra("preference_name")
            val preference = context?.getSharedPreferences(preferenceName, MODE_PRIVATE)
            notifyPreferenceChange(context, preference?.all ?: return)
        }
    }
}