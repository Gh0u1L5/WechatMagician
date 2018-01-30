package com.gh0u1l5.wechatmagician.backend

import android.content.Context
import android.content.res.XModuleResources
import android.os.Build
import com.gh0u1l5.wechatmagician.BuildConfig
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.FOLDER_SHARED
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_BASE_DIR
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_DEVELOPER
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SETTINGS
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_RESOURCES
import com.gh0u1l5.wechatmagician.Global.tryWithLog
import com.gh0u1l5.wechatmagician.Global.tryWithThread
import com.gh0u1l5.wechatmagician.backend.plugins.*
import com.gh0u1l5.wechatmagician.frontend.wechat.AdapterHider
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.list.ChatroomHideList
import com.gh0u1l5.wechatmagician.storage.list.SecretFriendList
import dalvik.system.PathClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

// WechatHook is the entry point of the module, here we load all the plugins.
class WechatHook : IXposedHookLoadPackage {

    companion object {
        @Volatile var MODULE_RES: XModuleResources? = null
    }

    private val hookThreadQueue: MutableList<Thread> = mutableListOf()

    private val settings = Preferences()
    private val developer = Preferences()

    // NOTE: Hooking Application.attach is necessary because Android 4.X is not supporting
    //       multi-dex applications natively. More information are available in this link:
    //       https://github.com/rovo89/xposedbridge/issues/30
    // NOTE: Since Wechat 6.5.16, the MultiDex installation became asynchronous. It is not
    //       guaranteed to be finished after Application.attach, but the exceptions caused
    //       by this can be ignored safely (See details in tryHook).
    private inline fun hookApplicationAttach(loader: ClassLoader, crossinline callback: (Context) -> Unit) {
        findAndHookMethod("android.app.Application", loader, "attach", C.Context, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                callback(param.thisObject as Context)
            }
        })
    }

    // isWechat returns true if the current application seems to be Wechat.
    private fun isWechat(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        val features = listOf (
                "libwechatcommon.so",
                "libwechatmm.so",
                "libwechatnetwork.so",
                "libwechatsight.so",
                "libwechatxlog.so"
        )
        return try {
            val libraryDir = File(lpparam.appInfo.nativeLibraryDir)
            val hits = features.filter { filename ->
                File(libraryDir, filename).exists()
            }.size
            (hits.toDouble() / features.size) > 0.5F
        } catch (t: Throwable) { false }
    }

    // NOTE: For Android 7.X or later, multi-thread and lazy initialization
    //       causes unexpected crashes with WeXposed. So I fall back to the
    //       original logic for now.
    private inline fun tryHook(crossinline hook: () -> Unit) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> tryWithLog { hook() }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> hookThreadQueue.add(tryWithThread { hook() })
            else -> hookThreadQueue.add(tryWithThread { try { hook() } catch (t: Throwable) { /* Ignore */ } })
        }
    }

    private fun loadModuleResource(context: Context) {
        hookThreadQueue.add(tryWithThread {
            val pm = context.packageManager
            val path = pm.getApplicationInfo(MAGICIAN_PACKAGE_NAME, 0).publicSourceDir
            MODULE_RES = XModuleResources.createInstance(path, null)
            WechatPackage.setStatus(STATUS_FLAG_RESOURCES, true)
        })
    }

    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        tryWithLog {
            when (lpparam.packageName) {
                MAGICIAN_PACKAGE_NAME ->
                    hookApplicationAttach(lpparam.classLoader, { _ ->
                        val pluginFrontend = Frontend
                        pluginFrontend.init(lpparam.classLoader)
                        pluginFrontend.notifyStatus()
                        pluginFrontend.setDirectoryPermissions()
                    })
                else -> if (isWechat(lpparam)) {
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

    // handleLoadWechat is the entry point for Wechat hooking logic.
    private fun handleLoadWechat(lpparam: XC_LoadPackage.LoadPackageParam, context: Context) {
        settings.init(PREFERENCE_NAME_SETTINGS)
        developer.init(PREFERENCE_NAME_DEVELOPER)

        WechatPackage.init(lpparam)
        LocalizedStrings.init(settings)
        SecretFriendList.init(context)
        ChatroomHideList.init(context)

        tryHook(WechatPackage::hookAdapters)
        tryHook(AdapterHider::hookAdaptersGetItem)
        tryHook(AdapterHider::hookAdaptersGetCount)
        tryHook(AdapterHider::hookAdapterNotifyChanged)

        val pluginDeveloper = Developer
        pluginDeveloper.init(developer)
        tryHook(pluginDeveloper::traceTouchEvents)
        tryHook(pluginDeveloper::traceActivities)
        tryHook(pluginDeveloper::dumpPopupMenu)
        tryHook(pluginDeveloper::enableXLog)
        tryHook(pluginDeveloper::traceDatabase)
        tryHook(pluginDeveloper::traceLogCat)
        tryHook(pluginDeveloper::traceXMLParse)

        val pluginStorage = Storage
        tryHook(pluginStorage::hookMsgStorage)
//        tryHook(pluginStorage::hookImgStorage)

        val pluginXML = XML
        pluginXML.init(settings)
        tryHook(pluginXML::hookXMLParse)

        val pluginDatabase = Database
        pluginDatabase.init(settings)
        tryHook(pluginDatabase::hookDatabase)

        val pluginUriRouter = UriRouter
        tryHook(pluginUriRouter::hijackUriRouter)

        val pluginSearchBar = SearchBar
        pluginSearchBar.init(settings)
        tryHook(pluginSearchBar::hijackSearchBar)

        val pluginPopupMenu = PopupMenu
        pluginPopupMenu.init(settings)
        tryHook(pluginPopupMenu::addMenuItemsForContacts)
        tryHook(pluginPopupMenu::addMenuItemsForConversations)

        val pluginAutoLogin = AutoLogin
        pluginAutoLogin.init(settings)
        tryHook(pluginAutoLogin::enableAutoLogin)

        val pluginSnsForward = SnsForward
        tryHook(pluginSnsForward::setLongClickableForSnsUserUI)
        tryHook(pluginSnsForward::setLongClickListenerForSnsUserUI)
        tryHook(pluginSnsForward::setLongClickListenerForSnsTimeLineUI)
        tryHook(pluginSnsForward::cleanTextViewBeforeForwarding)

        val pluginSecretFriend = SecretFriend
        pluginSecretFriend.init(settings)
        tryHook(pluginSecretFriend::hideChattingWindow)

        val pluginChatroomHider = ChatroomHider
        pluginChatroomHider.init(settings)

        val pluginLimits = Limits
        pluginLimits.init(settings)
        tryHook(pluginLimits::breakSelectPhotosLimit)
        tryHook(pluginLimits::breakSelectContactLimit)
        tryHook(pluginLimits::breakSelectConversationLimit)

        // Finish minor initializations
        settings.listen(context)
        developer.listen(context)
        loadModuleResource(context)
        WechatPackage.listen(context)

        // Write the status of all the hooks
        tryWithThread {
            // Wait until all the hook threads finished.
            hookThreadQueue.forEach { it.join() }
            WechatPackage.writeStatus("$MAGICIAN_BASE_DIR/$FOLDER_SHARED/status")
        }
    }

    // handleLoadWechatOnFly uses reflection to load updated module without reboot.
    private fun handleLoadWechatOnFly(lpparam: XC_LoadPackage.LoadPackageParam, context: Context) {
        (0 until 10).forEach { index ->
            val path = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                "/data/app/$MAGICIAN_PACKAGE_NAME-$index.apk"
            } else {
                "/data/app/$MAGICIAN_PACKAGE_NAME-$index/base.apk"
            }
            if (File(path).exists()) {
                val pathClassLoader = PathClassLoader(path, ClassLoader.getSystemClassLoader())
                val clazz = Class.forName("$MAGICIAN_PACKAGE_NAME.backend.WechatHook", true, pathClassLoader)
                val method = clazz.getDeclaredMethod("handleLoadWechat", lpparam.javaClass, Context::class.java)
                method.isAccessible = true
                method.invoke(clazz.newInstance(), lpparam, context); return
            }
        }
        log("Cannot load module on fly: APK not found")
    }
}
