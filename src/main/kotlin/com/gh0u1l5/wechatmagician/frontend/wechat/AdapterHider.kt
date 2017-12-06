package com.gh0u1l5.wechatmagician.frontend.wechat

import android.widget.BaseAdapter
import de.robv.android.xposed.XC_MethodHook
import java.util.concurrent.ConcurrentHashMap

object AdapterHider {

    // hideTable records the index of items we need to hide
    private val hideTable: MutableMap<BaseAdapter, Set<Int>> = ConcurrentHashMap()

    fun afterGetCount(param: XC_MethodHook.MethodHookParam) {
        val adapter = param.thisObject as BaseAdapter
        val count = param.result as Int
        val hideSize = hideTable[adapter]?.size ?: 0
        param.result = count - hideSize
    }

    fun beforeGetView(param: XC_MethodHook.MethodHookParam) {
        val adapter = param.thisObject as BaseAdapter
        val index = param.args[0] as Int
        val list = hideTable[adapter] ?: return
        param.args[0] =  index + list.filter { it <= index }.size
    }

    fun beforeNotifyDataSetChanged(param: XC_MethodHook.MethodHookParam, predicate: (Any?) -> Boolean) {
        val adapter = param.thisObject as BaseAdapter
        hideTable[adapter] = setOf()
        hideTable[adapter] = (0 until adapter.count).filter { index ->
            predicate(adapter.getItem(index))
        }.toSet()
    }

    fun notifyAdapter(adapter: Any?) {
        if (adapter is BaseAdapter) {
            adapter.notifyDataSetChanged()
        }
    }
}
