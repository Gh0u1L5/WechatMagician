package com.gh0u1l5.wechatmagician.spellbook.hookers

import android.view.View
import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.AddressAdapter
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.ConversationWithCacheAdapter
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.HeaderViewListAdapter
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.hookers.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.spellbook.util.C
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Adapters : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IAdapterHook::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        hookAllConstructors(AddressAdapter, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onAddressAdapterCreated") { plugin ->
                    if (plugin is IAdapterHook) {
                        val adapter = param.thisObject as? BaseAdapter
                        if (adapter != null) {
                            plugin.onAddressAdapterCreated(adapter)
                            return@notify
                        }
                        log("Expect address adapter to be BaseAdapter, get ${param.thisObject::class.java}")
                    }
                }
            }
        })
        hookAllConstructors(ConversationWithCacheAdapter, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onConversationAdapterCreated") { plugin ->
                    if (plugin is IAdapterHook) {
                        val adapter = param.thisObject as? BaseAdapter
                        if (adapter != null) {
                            plugin.onConversationAdapterCreated(adapter)
                            return@notify
                        }
                        log("Expect conversation adapter to be BaseAdapter, get ${param.thisObject::class.java}")
                    }
                }
            }
        })
        findAndHookMethod(
                HeaderViewListAdapter, "getView",
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