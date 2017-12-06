package com.gh0u1l5.wechatmagician.frontend.widget

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.support.v7.preference.EditTextPreference
import android.util.AttributeSet
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SETTINGS
import com.gh0u1l5.wechatmagician.util.PasswordUtil

class PasswordPreference : EditTextPreference {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onClick() {
        val pref = context.getSharedPreferences(PREFERENCE_NAME_SETTINGS, MODE_PRIVATE)
        PasswordUtil.changePassword(context, "Wechat Magician", pref, key)
    }
}