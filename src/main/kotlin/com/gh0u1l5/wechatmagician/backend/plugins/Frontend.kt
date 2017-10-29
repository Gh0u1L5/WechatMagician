package com.gh0u1l5.wechatmagician.backend.plugins

import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

class Frontend(private val loader: ClassLoader) {
    fun notifyStatus() {
        findAndHookMethod(
                "$MAGICIAN_PACKAGE_NAME.frontend.fragments.StatusFragment", loader,
                "isModuleLoaded", object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam): Any = true
        })
    }
}
