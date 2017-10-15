package com.gh0u1l5.wechatmagician.backend

import android.annotation.SuppressLint
import android.os.Environment
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.plugins.*
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.util.FileUtil
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import kotlin.concurrent.thread

// WechatHook is the entry point of the module, here we load all the plugins.
class WechatHook : IXposedHookLoadPackage {

    private val pkg = WechatPackage
    private val settings = Preferences(listOf("settings_sns_keyword_blacklist_content"))
    private val developer = Preferences()

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

    @SuppressLint("SetWorldReadable")
    private fun handleLoadWechat(lpparam: XC_LoadPackage.LoadPackageParam) {
        val loader = lpparam.classLoader
        pkg.init(lpparam)
        settings.load(XSharedPreferences(
                "com.gh0u1l5.wechatmagician", "settings"
        ))
        developer.load(XSharedPreferences(
                "com.gh0u1l5.wechatmagician", "developer"
        ), false)

        tryHook({
            val pluginDeveloper = Developer(loader, developer)
            pluginDeveloper.traceTouchEvents()
            pluginDeveloper.traceActivities()
            pluginDeveloper.enableXLog()
            pluginDeveloper.traceXMLParse()
            pluginDeveloper.traceDatabase()

            val pluginSnsUI = SnsUI(settings)
            pluginSnsUI.setItemLongPressPopupMenu()
            pluginSnsUI.cleanTextViewForForwarding()

            val pluginLimits = Limits(settings)
            pluginLimits.breakSelectPhotosLimit()
            pluginLimits.breakSelectContactLimit()
            pluginLimits.breakSelectConversationLimit()

            val pluginStorage = Storage(loader)
            pluginStorage.hookMsgStorage()
            pluginStorage.hookImgStorage()

            val pluginXML = XML(settings)
            pluginXML.hookXMLParse()

            val pluginDatabase = Database(settings)
            pluginDatabase.hookDatabase()

            val pluginCustomScheme = CustomScheme()
            pluginCustomScheme.registerCustomSchemes()
        })
        thread(start = true) {
            val storage = Environment.getExternalStorageDirectory().path + "/WechatMagician"
            FileUtil.writeOnce("$storage/.status/pkg", { path->
                FileUtil.writeBytesToDisk(path, pkg.toString().toByteArray())
                File(path).setReadable(true, false)
            })
            FileUtil.writeOnce("$storage/.status/hooks", { path ->
                FileUtil.writeObjectToDisk(path, pkg.status)
                File(path).setReadable(true, false)
            })
        }
    }
}
