package com.gh0u1l5.wechatmagician.frontend.wechat

import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Predicate
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
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

    data class Record(
            // The variable sections records the sections of items we want to show
            @Volatile var sections: List<Section>,
            // The variable predicates records the predicates of the adapters.
            // An item will be hidden if it satisfies any one of the predicates.
            @Volatile var predicates: Map<String, Predicate>
    )

    private val pkg = WechatPackage
    private val records: MutableMap<BaseAdapter, Record> = ConcurrentHashMap()

    fun register(adapter: BaseAdapter, predicateName: String, predicateBody: Predicate) {
        if (adapter in records) {
            records[adapter]!!.predicates += (predicateName to predicateBody)
        } else {
            records[adapter] = Record(emptyList(), mapOf(predicateName to predicateBody))
        }
    }

    private fun updateAdapterSections(param: XC_MethodHook.MethodHookParam) {
        val adapter = param.thisObject as BaseAdapter
        val record = records[adapter] ?: return

        record.sections = emptyList()
        val initial = listOf(Section(0, adapter.count, 0))
        val predicates = record.predicates.values
        record.sections = (0 until adapter.count).filter { index ->
            // Hide the items satisfying any one of the predicates
            val item = adapter.getItem(index)
            predicates.forEach { predicate ->
                if (predicate(item)) {
                    return@filter true
                }
            }
            return@filter false
        }.fold(initial, { sections, index ->
            sections.dropLast(1) + sections.last().split(index)
        })
    }

    @JvmStatic fun hookAdaptersGetItem() {
        findAndHookMethod(pkg.MMBaseAdapter, pkg.MMBaseAdapterGetMethod, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val adapter = param.thisObject as BaseAdapter
                val index = param.args[0] as Int
                val sections = records[adapter]?.sections ?: return
                sections.forEach { section ->
                    if (index in section) {
                        param.args[0] = section.base + (index - section.start)
                        return
                    }
                }
            }
        })
    }

    @JvmStatic fun hookAdaptersGetCount() {
        findAndHookMethod(pkg.MMBaseAdapter, "getCount", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val adapter = param.thisObject as BaseAdapter
                val sections = records[adapter]?.sections ?: return
                if (sections.isNotEmpty()) {
                    param.result = sections.sumBy { it.size() }
                }
            }
        })
    }

    @JvmStatic fun hookAdapterNotifyChanged() {
        findAndHookMethod(pkg.BaseAdapter, "notifyDataSetChanged", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                when (param.thisObject.javaClass) {
                    pkg.AddressAdapter -> {
                        updateAdapterSections(param)
                    }
                    pkg.ConversationWithCacheAdapter -> {
                        updateAdapterSections(param)
                    }
                }
            }
        })
    }
}
