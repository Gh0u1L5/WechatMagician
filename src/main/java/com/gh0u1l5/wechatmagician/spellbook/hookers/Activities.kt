package com.gh0u1l5.wechatmagician.spellbook.hookers

import android.app.Activity
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.gallery.ui.Classes.AlbumPreviewUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.sns.ui.Classes.SnsTimeLineUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.sns.ui.Classes.SnsUploadUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.sns.ui.Classes.SnsUserUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.webwx.ui.Classes.WebWXLoginUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.chatting.Classes.ChattingUI
import com.gh0u1l5.wechatmagician.spellbook.C
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Activities : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IActivityHook::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(AlbumPreviewUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                notify("onAlbumPreviewUICreated") { plugin ->
                    (plugin as IActivityHook).onAlbumPreviewUICreated(activity)
                }
            }
        })
        findAndHookMethod(ChattingUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                notify("onChattingUICreated") { plugin ->
                    (plugin as IActivityHook).onChattingUICreated(activity)
                }
            }
        })
        findAndHookMethod(WebWXLoginUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                notify("onWebLoginUICreated") { plugin ->
                    (plugin as IActivityHook).onWebLoginUICreated(activity)
                }
            }
        })
        findAndHookMethod(SnsTimeLineUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                notify("onSnsTimelineUICreated") { plugin ->
                    (plugin as IActivityHook).onSnsTimelineUICreated(activity)
                }
            }
        })
        findAndHookMethod(SnsUploadUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                notify("onSnsUploadUICreated") { plugin ->
                    (plugin as IActivityHook).onSnsUploadUICreated(activity)
                }
            }
        })
        findAndHookMethod(SnsUserUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                notify("onSnsUserUICreated") { plugin ->
                    (plugin as IActivityHook).onSnsUserUICreated(activity)
                }
            }
        })
    }
}