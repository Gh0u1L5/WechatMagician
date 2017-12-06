package com.gh0u1l5.wechatmagician.frontend.wechat

import android.widget.BaseAdapter
import java.util.concurrent.ConcurrentHashMap

object AdapterHider {

    // hideTable records the index of items we need to hide
    private val hideTable: MutableMap<BaseAdapter, Set<Int>> = ConcurrentHashMap()

    fun onGetCount(adapter: BaseAdapter, count: Int): Int {
        val hideSize = hideTable[adapter]?.size ?: 0
        return count - hideSize
    }

    fun onGetView(adapter: BaseAdapter, index: Int): Int {
        val list = hideTable[adapter] ?: return index
        return index + list.filter { it <= index }.size
    }

    fun onDataSetChanged(adapter: BaseAdapter, predicate: (Any?) -> Boolean) {
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
