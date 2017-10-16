package com.gh0u1l5.wechatmagician.backend

import android.annotation.SuppressLint
import android.os.Environment
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.plugins.*
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.util.FileUtil
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import kotlin.concurrent.thread

// WechatHook is the entry point of the module, here we load all the plugins.
class WechatHook : IXposedHookLoadPackage {

    private val pkg = WechatPackage
    private val settings = Preferences()
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
        val storage = Environment.getExternalStorageDirectory().path + "/WechatMagician"

        pkg.init(lpparam)
        settings.load("$storage/.prefs/settings")
        developer.load("$storage/.prefs/developer", false)

        val pluginDeveloper = Developer(loader, developer)
        tryHook(pluginDeveloper::traceTouchEvents)
        tryHook(pluginDeveloper::traceActivities)
        tryHook(pluginDeveloper::enableXLog)
        tryHook(pluginDeveloper::traceXMLParse)
        tryHook(pluginDeveloper::traceDatabase)

        val pluginSnsUI = SnsUI(settings)
        tryHook(pluginSnsUI::setItemLongPressPopupMenu)
        tryHook(pluginSnsUI::cleanTextViewForForwarding)

        val pluginLimits = Limits(settings)
        tryHook(pluginLimits::breakSelectPhotosLimit)
        tryHook(pluginLimits::breakSelectContactLimit)
        tryHook(pluginLimits::breakSelectConversationLimit)

        val pluginStorage = Storage(loader)
        tryHook(pluginStorage::hookMsgStorage)
        tryHook(pluginStorage::hookImgStorage)

        val pluginXML = XML(settings)
        tryHook(pluginXML::hookXMLParse)

        val pluginDatabase = Database(settings)
        tryHook(pluginDatabase::hookDatabase)

        val pluginCustomScheme = CustomScheme()
        tryHook(pluginCustomScheme::registerCustomSchemes)

        thread(start = true) {
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
