package com.gh0u1l5.wechatmagician.backend.plugins

import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

class Frontend(private val loader: ClassLoader) {
    fun notifyStatus() {
        findAndHookMethod(
                "com.gh0u1l5.wechatmagician.frontend.fragments.StatusFragment", loader,
                "isModuleLoaded", object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam): Any {
                return true
            }
        })
    }
}
