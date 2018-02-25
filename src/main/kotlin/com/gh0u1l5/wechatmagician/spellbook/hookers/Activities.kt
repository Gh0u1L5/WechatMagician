package com.gh0u1l5.wechatmagician.spellbook.hookers

import android.app.Activity
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.AlbumPreviewUI
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.ChattingUI
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SnsTimeLineUI
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SnsUploadUI
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SnsUserUI
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.WebWXLoginUI
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.hookers.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.util.C
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Activities : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IActivityHook::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(AlbumPreviewUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onAlbumPreviewUICreated") { plugin ->
                    if (plugin is IActivityHook) {
                        val activity = param.thisObject
                        if (activity is Activity) {
                            plugin.onAlbumPreviewUICreated(activity)
                        }
                    }
                }
            }
        })
        findAndHookMethod(ChattingUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onChattingUICreated") { plugin ->
                    if (plugin is IActivityHook) {
                        val activity = param.thisObject
                        if (activity is Activity) {
                            plugin.onChattingUICreated(activity)
                        }
                    }
                }
            }
        })
        findAndHookMethod(WebWXLoginUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onWebLoginUICreated") { plugin ->
                    if (plugin is IActivityHook) {
                        val activity = param.thisObject
                        if (activity is Activity) {
                            plugin.onWebLoginUICreated(activity)
                        }
                    }
                }
            }
        })
        findAndHookMethod(SnsTimeLineUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onSnsTimelineUICreated") { plugin ->
                    if (plugin is IActivityHook) {
                        val activity = param.thisObject
                        if (activity is Activity) {
                            plugin.onSnsTimelineUICreated(activity)
                        }
                    }
                }
            }
        })
        findAndHookMethod(SnsUploadUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onSnsUploadUICreated") { plugin ->
                    if (plugin is IActivityHook) {
                        val activity = param.thisObject
                        if (activity is Activity) {
                            plugin.onSnsUploadUICreated(activity)
                        }
                    }
                }
            }
        })
        findAndHookMethod(SnsUserUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onSnsUserUICreated") { plugin ->
                    if (plugin is IActivityHook) {
                        val activity = param.thisObject
                        if (activity is Activity) {
                            plugin.onSnsUserUICreated(activity)
                        }
                    }
                }
            }
        })
    }
}