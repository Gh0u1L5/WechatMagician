package com.gh0u1l5.wechatmagician.backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.XModuleResources
import android.os.Bundle
import com.gh0u1l5.wechatmagician.BuildConfig
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_HOOK_STATUS
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_WECHAT_PACKAGE
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_DEVELOPER
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SETTINGS
import com.gh0u1l5.wechatmagician.backend.plugins.*
import com.gh0u1l5.wechatmagician.backend.storage.Preferences
import com.gh0u1l5.wechatmagician.backend.storage.list.ChatroomHideList
import com.gh0u1l5.wechatmagician.backend.storage.list.SecretFriendList
import com.gh0u1l5.wechatmagician.spellbook.Global.STATUS_FLAG_RESOURCES
import com.gh0u1l5.wechatmagician.spellbook.SpellBook
import com.gh0u1l5.wechatmagician.spellbook.SpellBook.getApplicationApkPath
import com.gh0u1l5.wechatmagician.spellbook.SpellBook.isImportantWechatProcess
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryAsynchronously
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryVerbosely
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

        private val plugins = listOf (
                AdBlock,
                Alert,
                AntiRevoke,
                AntiSnsDelete,
                AutoLogin,
                ChatroomHider,
                Developer,
                Donate,
                Limits,
                MarkAllAsRead,
                ObjectsHunter,
                SecretFriend,
                SnsBlock,
                SnsForward
        )
        private val customHookers = listOf(Limits, Developer)

        private val requireHookStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                setResultExtras(Bundle().apply {
                    putSerializable("status", WechatStatus.report())
                })
            }
        }

        private val requireWechatPackageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                resultData = WechatPackage.toString()
            }
        }
    }

    // NOTE: Hooking Application.attach is necessary because Android 4.X is not supporting
    //       multi-dex applications natively. More information are available in this link:
    //       https://github.com/rovo89/xposedbridge/issues/30
    // NOTE: Since Wechat 6.5.16, the MultiDex installation became asynchronous. It is not
    //       guaranteed to be finished after Application.attach, but the exceptions caused
    //       by this can be ignored safely (See details in tryHook).
    private inline fun hookApplicationAttach(loader: ClassLoader, crossinline callback: (Context) -> Unit) {
        findAndHookMethod("android.app.Application", loader, "attach", Context::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                callback(param.thisObject as Context)
            }
        })
    }

    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        tryVerbosely {
            when (lpparam.packageName) {
                MAGICIAN_PACKAGE_NAME ->
                    hookApplicationAttach(lpparam.classLoader, { _ ->
                        handleLoadMagician(lpparam.classLoader)
                    })
                else -> if (isImportantWechatProcess(lpparam)) {
                    log("Wechat Magician: process = ${lpparam.processName}, version = ${BuildConfig.VERSION_NAME}")
                    hookApplicationAttach(lpparam.classLoader, { context ->
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
            context.registerReceiver(requireWechatPackageReceiver, IntentFilter(ACTION_REQUIRE_WECHAT_PACKAGE))
        }

        // Load module resources to current process
        tryAsynchronously {
            val path = getApplicationApkPath(MAGICIAN_PACKAGE_NAME)
            resources = XModuleResources.createInstance(path, null)
            WechatStatus.toggle(STATUS_FLAG_RESOURCES, true)
        }

        // Initialize the shared preferences
        settings.listen(context)
        settings.load(context)
        developer.listen(context)
        developer.load(context)

        // Initialize the localized strings and the lists generated by plugins
        SecretFriendList.load(context)
        ChatroomHideList.load(context)

        // Launch Wechat SpellBook
        SpellBook.startup(lpparam, plugins, customHookers)
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
