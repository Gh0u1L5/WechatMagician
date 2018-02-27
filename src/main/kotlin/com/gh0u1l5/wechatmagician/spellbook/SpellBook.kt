package com.gh0u1l5.wechatmagician.spellbook

import android.os.Build
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.hookers.*
import com.gh0u1l5.wechatmagician.spellbook.hookers.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryAsynchronously
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryVerbosely
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

object SpellBook {

    private val hookers: List<Any> = listOf (
            Activities,
            Adapters,
            Database,
            FileSystem,
            ListViewHider,
            MenuAppender,
            Notifications,
            SearchBar,
            Storage,
            UriRouter,
            XmlParser
    )

    private val centers: List<EventCenter> = hookers.mapNotNull { it as? EventCenter }

    // isImportantWechatProcess returns true if the current process seems to be an important Wechat process.
    // NOTE: Currently we only interested in main process and tools process
    fun isImportantWechatProcess(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        val processName = lpparam.processName
        if (processName.contains(':')) {
            if (!processName.endsWith(":tools")) {
                return false
            }
        }
        val features = listOf (
                "libwechatcommon.so",
                "libwechatmm.so",
                "libwechatnetwork.so",
                "libwechatsight.so",
                "libwechatxlog.so"
        )
        return try {
            val libraryDir = File(lpparam.appInfo.nativeLibraryDir)
            features.filter { filename ->
                File(libraryDir, filename).exists()
            }.size >= 3
        } catch (t: Throwable) { false }
    }

    // NOTE: For Android 7.X or later, multi-thread and lazy initialization
    //       causes unexpected crashes with WeXposed. So I fall back to the
    //       original logic for now.
    private inline fun tryHook(crossinline hook: () -> Unit) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> tryVerbosely { hook() }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> tryAsynchronously { hook() }
            else -> tryAsynchronously { try { hook() } catch (t: Throwable) { /* Ignore */ } }
        }
    }

    fun startup(lpparam: XC_LoadPackage.LoadPackageParam, plugins: List<Any>, customHookers: List<Any>) {
        WechatPackage.init(lpparam)

        loadPlugins(plugins)
        deployHookers(customHookers)
    }

    private fun loadPlugins(plugins: List<Any>) {
        centers.forEach { center ->
            tryAsynchronously {
                center.interfaces.forEach { `interface` ->
                    plugins.forEach { plugin ->
                        val assignable = `interface`.isAssignableFrom(plugin::class.java)
                        if (assignable) {
                            center.register(`interface`, plugin)
                        }
                    }
                }
            }
        }
    }

    private fun deployHookers(customHookers: List<Any>) {
        (hookers + customHookers).forEach { hooker ->
            hooker::class.java.declaredMethods.forEach { method ->
                val isHookMethod = method.isAnnotationPresent(WechatHookMethod::class.java)
                if (isHookMethod) {
                    tryHook { method.invoke(null) }
                }
            }
        }
    }
}