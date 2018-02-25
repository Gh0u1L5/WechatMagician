package com.gh0u1l5.wechatmagician.spellbook.interfaces

import android.app.Activity
import android.net.Uri

interface IUriRouterHook {
    fun onUriRouterReceive(activity: Activity, uri: Uri) { }
}