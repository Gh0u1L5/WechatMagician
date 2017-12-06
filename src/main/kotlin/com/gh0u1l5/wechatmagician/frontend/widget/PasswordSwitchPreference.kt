package com.gh0u1l5.wechatmagician.frontend.widget

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.support.v7.preference.SwitchPreferenceCompat
import android.util.AttributeSet
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SETTINGS
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_VERIFY_PASSWORD
import com.gh0u1l5.wechatmagician.util.PasswordUtil

class PasswordSwitchPreference : SwitchPreferenceCompat {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onClick() {
        val pref = context.getSharedPreferences(PREFERENCE_NAME_SETTINGS, MODE_PRIVATE)

        val status = pref.getBoolean(key, false)
        if (!status) {
            return super.onClick()
        }

        val encrypted = pref.getString("${key}_password", "")
        if (encrypted == "") {
            return super.onClick()
        }

        val message = LocalizedStrings[PROMPT_VERIFY_PASSWORD]
        PasswordUtil.askPasswordWithVerify(context, "Wechat Magician", message, encrypted) {
            super.onClick()
        }
    }
}