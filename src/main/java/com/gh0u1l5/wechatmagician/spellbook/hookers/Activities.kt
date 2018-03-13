package com.gh0u1l5.wechatmagician.spellbook.hookers

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.Classes.MMActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Activities : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IActivityHook::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(MMActivity, "onCreateOptionsMenu", C.Menu, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                val menu = param.args[0] as? Menu ?: return
                notify("onMMActivityOptionsMenuCreated") { plugin ->
                    (plugin as IActivityHook).onMMActivityOptionsMenuCreated(activity, menu)
                }
            }
        })
        findAndHookMethod(C.Activity, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                val savedInstanceState = param.args[0] as Bundle?
                notify("onActivityCreating") { plugin ->
                    (plugin as IActivityHook).onActivityCreating(activity, savedInstanceState)
                }
            }
        })
        findAndHookMethod(C.Activity, "onStart", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                notify("onActivityStarting") { plugin ->
                    (plugin as IActivityHook).onActivityStarting(activity)
                }
            }
        })
        findAndHookMethod(C.Activity, "onResume", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                notify("onActivityResuming") { plugin ->
                    (plugin as IActivityHook).onActivityResuming(activity)
                }
            }
        })
    }
}