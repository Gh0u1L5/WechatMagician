package com.gh0u1l5.wechatmagician.frontend.widget

import android.content.Context
import android.preference.EditTextPreference
import android.util.AttributeSet
import com.gh0u1l5.wechatmagician.util.PasswordUtil

class PasswordPreference : EditTextPreference {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onClick() {
        val pref = preferenceManager.sharedPreferences
        val encrypted = pref.getString(key, "")
        if (encrypted.isEmpty()) {
            PasswordUtil.createPassword(context, "Wechat Magician", pref, key)
        } else {
            PasswordUtil.changePassword(context, "Wechat Magician", pref, key)
        }
    }
}