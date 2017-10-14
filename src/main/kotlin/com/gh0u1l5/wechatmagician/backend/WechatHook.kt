package com.gh0u1l5.wechatmagician.backend

import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.plugins.*
import com.gh0u1l5.wechatmagician.storage.Preferences
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage

// WechatHook is the entry point of the module, here we load all the plugins.
class WechatHook : IXposedHookLoadPackage {

    private val pkg = WechatPackage
    private val preferences = Preferences()

    // NOTE: Hooking Application.attach is necessary because Android 4.X is not supporting
    //       multi-dex applications natively. More information are available in this link:
    //       https://github.com/rovo89/xposedbridge/issues/30
    private fun hookApplicationAttach(loader: ClassLoader, callback: () -> Unit) {
        findAndHookMethod("android.app.Application",loader, "attach", C.Context, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) { callback() }
        })
    }

    private fun tryHook(hook: () -> Unit) {
        try { hook() } catch (e: Throwable) { log("HOOK => $e") }
    }

    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            when (lpparam.packageName) {
                "com.gh0u1l5.wechatmagician" ->
                    hookApplicationAttach(lpparam.classLoader, {
                        val pluginFrontend = Frontend(lpparam.classLoader)
                        tryHook(pluginFrontend::notifyStatus)
                    })
                "com.tencent.mm" ->
                    hookApplicationAttach(lpparam.classLoader, {
                        handleLoadWechat(lpparam)
                    })
            }
        } catch (e: Throwable) { log(e) }
    }

    private fun handleLoadWechat(lpparam: XC_LoadPackage.LoadPackageParam) {
        val loader = lpparam.classLoader
        pkg.init(lpparam)
        pkg.dumpPackage()
        preferences.load(XSharedPreferences("com.gh0u1l5.wechatmagician"))

        tryHook({
            val pluginSystem = System(loader, preferences)
            pluginSystem.traceTouchEvents()
            pluginSystem.traceActivities()
            pluginSystem.enableXLog()

            val pluginSnsUI = SnsUI(preferences)
            pluginSnsUI.setItemLongPressPopupMenu()
            pluginSnsUI.cleanTextViewForForwarding()

            val pluginLimits = Limits(preferences)
            pluginLimits.breakSelectPhotosLimit()
            pluginLimits.breakSelectContactLimit()
            pluginLimits.breakSelectConversationLimit()

            val pluginStorage = Storage(loader)
            pluginStorage.hookMsgStorage()
            pluginStorage.hookImgStorage()

            val pluginXML = XML(preferences)
            pluginXML.hookXMLParse()

            val pluginDatabase = Database(loader, preferences)
            pluginDatabase.hookDatabase()
        })
    }
}
