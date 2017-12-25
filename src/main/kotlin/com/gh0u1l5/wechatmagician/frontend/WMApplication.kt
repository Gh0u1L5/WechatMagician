package com.gh0u1l5.wechatmagician.frontend

import android.content.Context
import android.content.res.Configuration
import android.support.multidex.MultiDexApplication
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SETTINGS
import com.gh0u1l5.wechatmagician.Global.SETTINGS_MODULE_LANGUAGE
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.util.LocaleUtil
import com.gh0u1l5.wechatmagician.util.ViewUtil.getDefaultLanguage

class WMApplication: MultiDexApplication() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtil.onAttach(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        LocaleUtil.onAttach(this)
    }
}