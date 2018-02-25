package com.gh0u1l5.wechatmagician.backend.storage.list

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryAsynchronously

open class PersistentList(private val preferenceName: String) : BaseList<String>() {

    @Volatile var preference: SharedPreferences? = null

    fun load(context: Context) {
        tryAsynchronously {
            preference = context.getSharedPreferences(preferenceName, MODE_PRIVATE)
            this += preference!!.all.filter { it.value == true }.keys
        }
    }

    override operator fun plusAssign(value: String) {
        super.plusAssign(value)
        preference?.edit()?.putBoolean(value, true)?.apply()
    }

    override operator fun minusAssign(value: String) {
        super.minusAssign(value)
        preference?.edit()?.putBoolean(value, false)?.apply()
    }
}