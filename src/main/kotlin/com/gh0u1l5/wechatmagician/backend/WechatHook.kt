package com.gh0u1l5.wechatmagician.backend

import android.content.Context
import android.os.Build
import com.gh0u1l5.wechatmagician.BuildConfig
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.FOLDER_SHARED
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_BASE_DIR
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_DEVELOPER
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SETTINGS
import com.gh0u1l5.wechatmagician.Global.SETTINGS_CUSTOM_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.WECHAT_PACKAGE_NAME
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
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import kotlin.concurrent.thread

// WechatHook is the entry point of the module, here we load all the plugins.
class WechatHook : IXposedHookLoadPackage {

    private val hookThreadQueue: MutableList<Thread> = mutableListOf()

    private val settings by lazy { Preferences(PREFERENCE_NAME_SETTINGS) }
    private val developer by lazy { Preferences(PREFERENCE_NAME_DEVELOPER) }


    // NOTE: Hooking Application.attach is necessary because Android 4.X is not supporting
    //       multi-dex applications natively. More information are available in this link:
    //       https://github.com/rovo89/xposedbridge/issues/30
    private inline fun hookApplicationAttach(loader: ClassLoader, crossinline callback: (Context) -> Unit) {
        findAndHookMethod("android.app.Application", loader, "attach", C.Context, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                callback(param.thisObject as Context)
            }
        })
    }

    private inline fun tryHook(crossinline hook: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try { hook() } catch (t: Throwable) { log(t) }
        } else {
            hookThreadQueue.add(thread(start = true) {
                hook()
            }.apply {
                setUncaughtExceptionHandler { _, t -> log(t) }
            })
        }
    }

    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            when (lpparam.packageName) {
                MAGICIAN_PACKAGE_NAME ->
                    hookApplicationAttach(lpparam.classLoader, { context ->
                        val pluginFrontend = Frontend
                        pluginFrontend.init(lpparam.classLoader)
                        pluginFrontend.notifyStatus()
                        pluginFrontend.setDirectoryPermissions(context)
                    })
                else ->
                    hookApplicationAttach(lpparam.classLoader, { context ->
                        if (!BuildConfig.DEBUG) {
                            handleLoadWechat(lpparam, context)
                        } else {
                            handleLoadWechatOnFly(lpparam, context)
                        }
                    })
            }
        } catch (e: Throwable) { log(e) }
    }

    private fun handleLoadWechat(lpparam: XC_LoadPackage.LoadPackageParam, context: Context) {
        val customPackageName = settings.getString(SETTINGS_CUSTOM_PACKAGE_NAME, WECHAT_PACKAGE_NAME)
        if (customPackageName != lpparam.packageName) {
            if (!Regex(customPackageName).matches(lpparam.packageName)) {
                return
            }
        }

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
        WechatResHook.MODULE_RES?.hashCode()

        // Wait until all the hook threads finished
        hookThreadQueue.forEach { it.join() }

        // Write the status of all the hooks
        WechatPackage.writeStatus("$MAGICIAN_BASE_DIR/$FOLDER_SHARED/status")
    }

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
