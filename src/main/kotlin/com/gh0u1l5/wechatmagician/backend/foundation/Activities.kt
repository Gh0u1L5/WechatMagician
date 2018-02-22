package com.gh0u1l5.wechatmagician.backend.foundation

import android.app.Activity
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.foundation.base.EventCenter
import com.gh0u1l5.wechatmagician.backend.interfaces.IActivityHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Activities : EventCenter() {

    private val pkg = WechatPackage

    @JvmStatic fun hookEvents() {
        findAndHookMethod(pkg.AlbumPreviewUI, "onCreate", C.Bundle, object : XC_MethodHook() {
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
        findAndHookMethod(pkg.ChattingUI, "onCreate", C.Bundle, object : XC_MethodHook() {
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
        findAndHookMethod(pkg.WebWXLoginUI, "onCreate", C.Bundle, object : XC_MethodHook() {
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
        findAndHookMethod(pkg.SnsTimeLineUI, "onCreate", C.Bundle, object : XC_MethodHook() {
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
        findAndHookMethod(pkg.SnsUploadUI, "onCreate", C.Bundle, object : XC_MethodHook() {
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
        findAndHookMethod(pkg.SnsUserUI, "onCreate", C.Bundle, object : XC_MethodHook() {
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