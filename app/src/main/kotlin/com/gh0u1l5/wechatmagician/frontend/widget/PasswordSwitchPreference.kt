package com.gh0u1l5.wechatmagician.frontend.widget

import android.content.Context
import android.preference.SwitchPreference
import android.util.AttributeSet
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.util.PasswordUtil

class PasswordSwitchPreference : SwitchPreference {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onClick() {
        val pref = preferenceManager.sharedPreferences
        val encrypted = pref.getString("${key}_password", "")

        val status = pref.getBoolean(key, false)
        if (status) { // close
            if (encrypted.isEmpty()) {
                return super.onClick()
            }
            val message = context.getString(R.string.prompt_verify_password)
            PasswordUtil.askPasswordWithVerify(context, "Wechat Magician", message, encrypted) {
                super.onClick()
            }
        } else { // open
            if (encrypted.isNotEmpty()) {
                return super.onClick()
            }
            PasswordUtil.createPassword(context, "Wechat Magician", pref, "${key}_password") {
                super.onClick()
            }
        }
    }
}