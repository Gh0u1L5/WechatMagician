package com.gh0u1l5.wechatmagician.spellbook.util

import de.robv.android.xposed.XposedBridge
import kotlin.concurrent.thread

object BasicUtil {
    fun <T: Any>tryVerbosely(func: () -> T?): T? {
        return try { func() } catch (t: Throwable) {
            XposedBridge.log(t); null
        }
    }

    fun tryAsynchronously(func: () -> Unit): Thread {
        return thread(start = true) { func() }.apply {
            setUncaughtExceptionHandler { _, t -> XposedBridge.log(t) }
        }
    }
}