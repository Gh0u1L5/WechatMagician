package com.gh0u1l5.wechatmagician.backend

import android.content.Context
import android.os.Environment
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_HOOKING
import com.gh0u1l5.wechatmagician.Global.WECHAT_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.backend.plugins.*
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.util.FileUtil
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlin.concurrent.thread

// WechatHook is the entry point of the module, here we load all the plugins.
class WechatHook : IXposedHookLoadPackage {

    private val pkg = WechatPackage
    private val status = WechatStatus
    private val settings = Preferences()
    private val developer = Preferences()

    // NOTE: Hooking Application.attach is necessary because Android 4.X is not supporting
    //       multi-dex applications natively. More information are available in this link:
    //       https://github.com/rovo89/xposedbridge/issues/30
    private fun hookApplicationAttach(loader: ClassLoader, callback: (Context?) -> Unit) {
        findAndHookMethod("android.app.Application",loader, "attach", C.Context, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                callback(param.thisObject as Context?)
            }
        })
    }

    private fun tryHook(hook: () -> Unit) {
        try { hook() } catch (e: Throwable) { log("HOOK => $e") }
    }

    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            if (lpparam.packageName == WECHAT_PACKAGE_NAME) {
                hookApplicationAttach(lpparam.classLoader, { context ->
                    handleLoadWechat(lpparam, context)
                })
            }
        } catch (e: Throwable) { log(e) }
    }

    private fun handleLoadWechat(lpparam: XC_LoadPackage.LoadPackageParam, context: Context?) {
        val loader = lpparam.classLoader

        pkg.init(lpparam)
        status.listen(context)
        status[STATUS_FLAG_HOOKING] = true
        settings.load(context, "settings")
        developer.load(context, "developer")

        thread(start = true) {
            // Note: The developer settings must be valid before hooking the functions,
            //       so we write a “spinlock" here to wait the update.
            while (!developer.loaded);
            val pluginDeveloper = Developer(loader, developer)
            tryHook(pluginDeveloper::traceTouchEvents)
            tryHook(pluginDeveloper::traceActivities)
            tryHook(pluginDeveloper::enableXLog)
            tryHook(pluginDeveloper::traceXMLParse)
            tryHook(pluginDeveloper::traceDatabase)
        }

        thread(start = true) {
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
        }

        // Note: This operation may fail if the Wechat does not have the permission to
        //       write external storage. So we put this at the end to make sure it will
        //       not interrupt the hooking logic.
        thread(start = true) {
            val storage = Environment.getExternalStorageDirectory().absolutePath + "/WechatMagician"
            FileUtil.writeOnce("$storage/.status/pkg", { path->
                FileUtil.writeBytesToDisk(path, pkg.toString().toByteArray())
            })
        }
    }
}
