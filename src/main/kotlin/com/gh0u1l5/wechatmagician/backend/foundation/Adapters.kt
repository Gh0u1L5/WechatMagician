package com.gh0u1l5.wechatmagician.backend.foundation

import android.view.View
import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.foundation.base.EventCenter
import com.gh0u1l5.wechatmagician.backend.interfaces.IAdapterHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Adapters : EventCenter() {

    private val pkg = WechatPackage

    @JvmStatic fun hookEvents() {
        hookAllConstructors(pkg.AddressAdapter, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onAddressAdapterCreated") { plugin ->
                    if (plugin is IAdapterHook) {
                        val adapter = param.thisObject as? BaseAdapter
                        if (adapter != null) {
                            plugin.onAddressAdapterCreated(adapter)
                            return@notify
                        }
                        log("Expect address adapter to be BaseAdapter, get ${param.thisObject.javaClass}")
                    }
                }
            }
        })
        hookAllConstructors(pkg.ConversationWithCacheAdapter, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onConversationAdapterCreated") { plugin ->
                    if (plugin is IAdapterHook) {
                        val adapter = param.thisObject as? BaseAdapter
                        if (adapter != null) {
                            plugin.onConversationAdapterCreated(adapter)
                            return@notify
                        }
                        log("Expect conversation adapter to be BaseAdapter, get ${param.thisObject.javaClass}")
                    }
                }
            }
        })
        findAndHookMethod(
                pkg.HeaderViewListAdapter, "getView",
                C.Int, C.View, C.ViewGroup, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onSnsUserUIAdapterGetView") { plugin ->
                    if (plugin is IAdapterHook) {
                        val adapter = param.thisObject
                        val convertView = param.args[1] as View?
                        val view = param.result as View?
                        if (view != null) {
                            plugin.onSnsUserUIAdapterGetView(adapter, convertView, view)
                        }
                    }
                }
            }
        })
    }
}