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
                val adapter = param.thisObject as? BaseAdapter
                if (adapter == null) {
                    log("Expect address adapter to be BaseAdapter, get ${param.thisObject::class.java}")
                    return
                }
                notify("onAddressAdapterCreated") { plugin ->
                    (plugin as IAdapterHook).onAddressAdapterCreated(adapter)
                }
            }
        })
        hookAllConstructors(ConversationWithCacheAdapter, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val adapter = param.thisObject as? BaseAdapter
                if (adapter == null) {
                    log("Expect conversation adapter to be BaseAdapter, get ${param.thisObject::class.java}")
                    return
                }
                notify("onConversationAdapterCreated") { plugin ->
                    (plugin as IAdapterHook).onConversationAdapterCreated(adapter)
                }
            }
        })
        findAndHookMethod(
                HeaderViewListAdapter, "getView",
                C.Int, C.View, C.ViewGroup, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val adapter = param.thisObject
                val convertView = param.args[1] as View?
                val view = param.result as View? ?: return
                notify("onSnsUserUIAdapterGotView") { plugin ->
                    (plugin as IAdapterHook).onSnsUserUIAdapterGotView(adapter, convertView, view)
                }
            }
        })
    }
}