package com.gh0u1l5.wechatmagician.frontend

import android.support.multidex.MultiDexApplication
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.util.ViewUtil.getDefaultLanguage

class WMApplication: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        LocalizedStrings.language = resources.getDefaultLanguage()
    }
}