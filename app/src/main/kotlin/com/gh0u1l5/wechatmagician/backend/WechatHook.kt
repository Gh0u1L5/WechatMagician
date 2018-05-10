package com.gh0u1l5.wechatmagician.backend

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.XModuleResources
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.gh0u1l5.wechatmagician.BuildConfig
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_HOOK_STATUS
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_REPORTS
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_DEVELOPER
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SETTINGS
import com.gh0u1l5.wechatmagician.backend.plugins.Developer
import com.gh0u1l5.wechatmagician.backend.storage.Preferences
import com.gh0u1l5.wechatmagician.backend.storage.list.ChatroomHideList
import com.gh0u1l5.wechatmagician.backend.storage.list.SecretFriendList
import com.gh0u1l5.wechatmagician.spellbook.SpellBook
import com.gh0u1l5.wechatmagician.spellbook.SpellBook.getApplicationApkPath
import com.gh0u1l5.wechatmagician.spellbook.SpellBook.isImportantWechatProcess
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxVersion
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.mirror.MirrorClasses
import com.gh0u1l5.wechatmagician.spellbook.mirror.MirrorFields
import com.gh0u1l5.wechatmagician.spellbook.mirror.MirrorMethods
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryAsynchronously
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryVerbosely
import com.gh0u1l5.wechatmagician.spellbook.util.FileUtil
import com.gh0u1l5.wechatmagician.spellbook.util.FileUtil.createTimeTag
import com.gh0u1l5.wechatmagician.spellbook.util.MirrorUtil.generateReport
import dalvik.system.PathClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

// WechatHook is the entry point of the module, here we load all the plugins.
class WechatHook : IXposedHookLoadPackage {

    companion object {
        @Volatile var resources: XModuleResources? = null

        val settings = Preferences(PREFERENCE_NAME_SETTINGS)
        val developer = Preferences(PREFERENCE_NAME_DEVELOPER)

        private val requireHookStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                setResultExtras(Bundle().apply {
                    putIntArray("status", WechatStatus.report())
                })
            }
        }

        private val requireMagicianReportReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val state = Environment.getExternalStorageState()
                if (state != MEDIA_MOUNTED) {
                    val message = "Error: SD card is not presented! (state: $state)"
                    Toast.makeText(context, message, LENGTH_SHORT).show()
                }

                val storage = Environment.getExternalStorageDirectory().absolutePath
                val reportPath = "$storage/WechatMagician/reports/report-${createTimeTag()}.txt"

                tryAsynchronously {
                    val reportHead = listOf (
                            "Device: SDK${Build.VERSION.SDK_INT}-${Build.PRODUCT}",
                            "Xposed Version: ${XposedBridge.XPOSED_BRIDGE_VERSION}",
                            "Wechat Version: $wxVersion",
                            "Module Version: ${BuildConfig.VERSION_NAME}"
                    ).joinToString("\n")
                    val reportBody = listOf(
                            "Classes:",
                            generateReport(MirrorClasses).joinToString("\n") {
                                "  ${it.first} -> ${it.second}"
                            },
                            "Methods:",
                            generateReport(MirrorMethods).joinToString("\n") {
                                "  ${it.first} -> ${it.second}"
                            },
                            "Fields:",
                            generateReport(MirrorFields).joinToString("\n") {
                                "  ${it.first} -> ${it.second}"
                            }
                    ).joinToString("\n")
                    FileUtil.writeBytesToDisk(reportPath, "$reportHead\n$reportBody".toByteArray())
                }

                resultData = reportPath
            }
        }
    }

    // hookAttachBaseContext is a stable way to get current application on all the platforms.
    private inline fun hookAttachBaseContext(loader: ClassLoader, crossinline callback: (Context) -> Unit) {
        findAndHookMethod(
                "android.content.ContextWrapper", loader, "attachBaseContext",
                Context::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                callback(param.thisObject as? Application ?: return)
            }
        })
    }

    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        tryVerbosely {
            when (lpparam.packageName) {
                MAGICIAN_PACKAGE_NAME ->
                    hookAttachBaseContext(lpparam.classLoader, { _ ->
                        handleLoadMagician(lpparam.classLoader)
                    })
                else -> if (isImportantWechatProcess(lpparam)) {
                    log("Wechat Magician: process = ${lpparam.processName}, version = ${BuildConfig.VERSION_NAME}")
                    hookAttachBaseContext(lpparam.classLoader, { context ->
                        if (!BuildConfig.DEBUG) {
                            handleLoadWechat(lpparam, context)
                        } else {
                            handleLoadWechatOnFly(lpparam, context)
                        }
                    })
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun handleLoadMagician(loader: ClassLoader) {
        findAndHookMethod(
                "$MAGICIAN_PACKAGE_NAME.frontend.fragments.StatusFragment", loader,
                "isModuleLoaded", object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam): Any = true
        })
        findAndHookMethod(
                "$MAGICIAN_PACKAGE_NAME.frontend.fragments.StatusFragment", loader,
                "getXposedVersion", object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam): Any = XposedBridge.XPOSED_BRIDGE_VERSION
        })
    }

    // handleLoadWechat is the entry point for Wechat hooking logic.
    private fun handleLoadWechat(lpparam: XC_LoadPackage.LoadPackageParam, context: Context) {
        // Register receivers for frontend communications
        tryVerbosely {
            context.registerReceiver(requireHookStatusReceiver, IntentFilter(ACTION_REQUIRE_HOOK_STATUS))
            context.registerReceiver(requireMagicianReportReceiver, IntentFilter(ACTION_REQUIRE_REPORTS))
        }

        // Load module resources to current process
        tryAsynchronously {
            val path = getApplicationApkPath(MAGICIAN_PACKAGE_NAME)
            resources = XModuleResources.createInstance(path, null)
            WechatStatus.toggle(WechatStatus.StatusFlag.STATUS_FLAG_RESOURCES)
        }

        // Initialize the shared preferences
        // TODO: check why it no longer works after restarting Wechat
        settings.listen(context)
        settings.load(context)
        developer.listen(context)
        developer.load(context)

        // Initialize the localized strings and the lists generated by plugins
        SecretFriendList.load(context)
        ChatroomHideList.load(context)

        // Launch Wechat SpellBook
        if (BuildConfig.DEBUG) {
            SpellBook.startup(lpparam, WechatPlugins)
        } else {
            SpellBook.startup(lpparam, WechatPlugins - Developer)
        }
    }

    // handleLoadWechatOnFly uses reflection to load updated module without reboot.
    private fun handleLoadWechatOnFly(lpparam: XC_LoadPackage.LoadPackageParam, context: Context) {
        val path = getApplicationApkPath(MAGICIAN_PACKAGE_NAME)
        if (!File(path).exists()) {
            log("Cannot load module on fly: APK not found")
            return
        }
        val pathClassLoader = PathClassLoader(path, ClassLoader.getSystemClassLoader())
        val clazz = Class.forName("$MAGICIAN_PACKAGE_NAME.backend.WechatHook", true, pathClassLoader)
        val method = clazz.getDeclaredMethod("handleLoadWechat", lpparam::class.java, Context::class.java)
        method.isAccessible = true
        method.invoke(clazz.newInstance(), lpparam, context)
    }
}
