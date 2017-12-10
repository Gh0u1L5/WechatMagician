package com.gh0u1l5.wechatmagician.frontend.wechat

import android.widget.BaseAdapter
import de.robv.android.xposed.XC_MethodHook
import java.util.concurrent.ConcurrentHashMap

object AdapterHider {

    data class Section(
            val start: Int,  // Inclusive
            val end: Int,    // Exclusive
            val base: Int
    ) {
        operator fun contains(index: Int) = (start <= index) && (index < end)

        fun size() = end - start

        fun split(index: Int): List<Section> {
            val length = index - base
            return listOf(
                    Section(start, start + length, base),
                    Section(start + length, end - 1, base + length + 1)
            ).filter { it.size() != 0 }
        }
    }

    // adapterMap records the sections of items we want to show
    private val adapterMap: MutableMap<BaseAdapter, List<Section>> = ConcurrentHashMap()

    fun afterGetCount(param: XC_MethodHook.MethodHookParam) {
        val adapter = param.thisObject as BaseAdapter
        val sections = adapterMap[adapter] ?: return
        if (sections.isNotEmpty()) {
            param.result = sections.sumBy { it.size() }
        }
    }

    fun beforeGetItem(param: XC_MethodHook.MethodHookParam) {
        val adapter = param.thisObject as BaseAdapter
        val index = param.args[0] as Int
        val sections = adapterMap[adapter] ?: return
        sections.forEach { section ->
            if (index in section) {
                param.args[0] = section.base + (index - section.start)
                return
            }
        }
    }

    fun beforeNotifyDataSetChanged(param: XC_MethodHook.MethodHookParam, predicate: (Any?) -> Boolean) {
        val adapter = param.thisObject as BaseAdapter
        adapterMap[adapter] = listOf()

        val initial = listOf(Section(0, adapter.count, 0))
        adapterMap[adapter] = (0 until adapter.count).filter { index ->
            predicate(adapter.getItem(index))
        }.fold(initial, { sections, index ->
            sections.dropLast(1) + sections.last().split(index)
        })
    }

    fun notifyAdapter(adapter: Any?) {
        if (adapter is BaseAdapter) {
            adapter.notifyDataSetChanged()
        }
    }
}
