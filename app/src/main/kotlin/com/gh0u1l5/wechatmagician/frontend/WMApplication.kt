package com.gh0u1l5.wechatmagician.frontend

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.gh0u1l5.wechatmagician.util.LocaleUtil

class WMApplication: Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtil.onAttach(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        LocaleUtil.onAttach(this)
    }
}