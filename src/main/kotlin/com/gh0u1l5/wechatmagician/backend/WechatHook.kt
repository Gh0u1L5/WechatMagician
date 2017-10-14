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
import kotlin.concurrent.thread

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

    private fun tryHook(hook: () -> Unit, cleanup: (Throwable) -> Unit) {
        try { hook() } catch (e: Throwable) { log("HOOK => $e"); cleanup(e) }
    }

    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            when (lpparam.packageName) {
                "com.gh0u1l5.wechatmagician" ->
                    hookApplicationAttach(lpparam.classLoader, {
                        val pluginFrontend = Frontend(lpparam.classLoader)
                        tryHook(pluginFrontend::notifyStatus, {})
                    })
                "com.tencent.mm" ->
                    hookApplicationAttach(lpparam.classLoader, {
                        handleLoadWechat(lpparam)
                    })
            }
        } catch (e: Throwable) {
            log("INIT => ${e.stackTrace}")
        }
    }

    private fun handleLoadWechat(lpparam: XC_LoadPackage.LoadPackageParam) {
        val loader = lpparam.classLoader
        pkg.init(lpparam)
        preferences.load(XSharedPreferences("com.gh0u1l5.wechatmagician"))

        val process = lpparam.processName
        if (process == "com.tencent.mm") {
            thread(start = true) {
                pkg.dumpPackage()
            }
        }

        val pluginSystem = System(loader, preferences)
        tryHook(pluginSystem::traceTouchEvents, {})
        tryHook(pluginSystem::traceActivities, {})
        tryHook(pluginSystem::enableXLog, {
            pkg.XLogSetup = null
        })

        val pluginSnsUI = SnsUI(preferences)
        tryHook(pluginSnsUI::setItemLongPressPopupMenu, {
            pkg.AdFrameLayout = null
        })
        tryHook(pluginSnsUI::cleanTextViewForForwarding, {
            pkg.SnsUploadUI = null
        })

        val pluginLimits = Limits(preferences)
        tryHook(pluginLimits::breakSelectPhotosLimit, {
            pkg.AlbumPreviewUI = null
        })
        tryHook(pluginLimits::breakSelectContactLimit, {
            pkg.SelectContactUI = null
        })
        tryHook(pluginLimits::breakSelectConversationLimit, {
            pkg.SelectConversationUI = null
        })

        val pluginStorage = Storage(loader)
        tryHook(pluginStorage::hookMsgStorage, {
            pkg.MsgStorageClass = null
        })
        tryHook(pluginStorage::hookImgStorage, {
            pkg.ImgStorageClass = null
        })

        val pluginXML = XML(preferences)
        tryHook(pluginXML::hookXMLParse, {
            pkg.XMLParserClass = null
        })

        val pluginDatabase = Database(loader, preferences)
        tryHook(pluginDatabase::hookDatabase, {
            pkg.SQLiteDatabaseClass = null
        })
    }
}
