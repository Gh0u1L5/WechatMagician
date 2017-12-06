package com.gh0u1l5.wechatmagician.storage.list

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SECRET_FRIEND
import kotlin.concurrent.thread

object SecretFriendList : BaseList<String?>() {
    // Load existing secret friend list from shared preferences
    fun init(context: Context) {
        thread(start = true) {
            val pref = context.getSharedPreferences(PREFERENCE_NAME_SECRET_FRIEND, MODE_PRIVATE)
            this += pref.all.filter { it.value == true }.keys
        }
    }
}