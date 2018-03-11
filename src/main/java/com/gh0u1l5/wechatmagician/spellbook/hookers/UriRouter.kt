package com.gh0u1l5.wechatmagician.spellbook.hookers

import android.app.Activity
import android.content.Intent
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IUriRouterHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.base.stub.Classes.WXCustomScheme
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.base.stub.Methods.WXCustomScheme_entry
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook

object UriRouter : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IUriRouterHook::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(WXCustomScheme, WXCustomScheme_entry, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = param.args[0] as Intent?
                val uri = intent?.data ?: return
                val activity = param.thisObject as Activity
                if (uri.host == "magician") {
                    notify("onUriRouterReceiving") { plugin ->
                        (plugin as IUriRouterHook).onUriRouterReceiving(activity, uri)
                    }
                    param.result = false
                }
            }
        })

        WechatStatus.toggle(WechatStatus.StatusFlag.STATUS_FLAG_URI_ROUTER, true)
    }
}