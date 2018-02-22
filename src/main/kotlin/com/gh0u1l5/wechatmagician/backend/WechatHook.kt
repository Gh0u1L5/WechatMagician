package com.gh0u1l5.wechatmagician.backend

import android.content.Context
import android.content.Intent
import android.content.res.XModuleResources
import android.os.Build
import com.gh0u1l5.wechatmagician.BuildConfig
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.ACTION_WECHAT_STARTUP
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_DEVELOPER
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SETTINGS
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_RESOURCES
import com.gh0u1l5.wechatmagician.Global.tryVerbosely
import com.gh0u1l5.wechatmagician.Global.tryAsynchronously
import com.gh0u1l5.wechatmagician.backend.foundation.*
import com.gh0u1l5.wechatmagician.backend.interfaces.*
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.list.ChatroomHideList
import com.gh0u1l5.wechatmagician.storage.list.SecretFriendList
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
    }

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

    // isImportantWechatProcesses returns true if the current process seems to be the main/tools process of Wechat.
    private fun isImportantWechatProcesses(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        val processName = lpparam.processName
        if (processName.contains(':')) {
            if (!processName.endsWith(":tools")) {
                // Currently we only interested in main process and tools process
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

    private fun findAPKPath(context: Context, packageName: String) =
            context.packageManager.getApplicationInfo(packageName, 0).publicSourceDir

    private fun loadPlugins() {
        tryAsynchronously {
            PluginList.forEach { plugin ->
                if (plugin is IActivityHook) {
                    Activities.register(IActivityHook::class.java, plugin)
                }
                if (plugin is IAdapterHook) {
                    Adapters.register(IAdapterHook::class.java, plugin)
                }
                if (plugin is IDatabaseHook) {
                    Database.register(IDatabaseHook::class.java, plugin)
                }
                if (plugin is IDatabaseHookRaw) {
                    Database.register(IDatabaseHookRaw::class.java, plugin)
                }
                if (plugin is IPopupMenuHook) {
                    MenuAppender.register(IPopupMenuHook::class.java, plugin)
                }
                if (plugin is ISearchBarConsole) {
                    SearchBar.register(ISearchBarConsole::class.java, plugin)
                }
                if (plugin is IUriRouterHook) {
                    UriRouter.register(IUriRouterHook::class.java, plugin)
                }
                if (plugin is IXmlParserHook) {
                    XmlParser.register(IXmlParserHook::class.java, plugin)
                }
                if (plugin is IXmlParserHookRaw) {
                    XmlParser.register(IXmlParserHookRaw::class.java, plugin)
                }
            }
        }
    }

    private fun loadModuleResource(context: Context) {
        tryAsynchronously {
            val path = findAPKPath(context, MAGICIAN_PACKAGE_NAME)
            resources = XModuleResources.createInstance(path, null)
            WechatPackage.setStatus(STATUS_FLAG_RESOURCES, true)
        }
    }

    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        tryVerbosely {
            when (lpparam.packageName) {
                MAGICIAN_PACKAGE_NAME ->
                    hookApplicationAttach(lpparam.classLoader, { _ ->
                        handleLoadMagician(lpparam.classLoader)
                    })
                else -> if (isImportantWechatProcesses(lpparam)) {
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
        context.sendBroadcast(Intent().setAction(ACTION_WECHAT_STARTUP))

        settings.listen(context)
        settings.load()
        developer.listen(context)
        developer.load()

        WechatPackage.init(lpparam)
        WechatPackage.listen(context)

        LocalizedStrings.init(settings)
        SecretFriendList.init(context)
        ChatroomHideList.init(context)

        // Setup foundational hooks
        HookList.forEach { tryHook(it) }

        // Load plugins and module resource
        loadPlugins()
        loadModuleResource(context)
    }

    // handleLoadWechatOnFly uses reflection to load updated module without reboot.
    private fun handleLoadWechatOnFly(lpparam: XC_LoadPackage.LoadPackageParam, context: Context) {
        val path = findAPKPath(context, MAGICIAN_PACKAGE_NAME)
        if (!File(path).exists()) {
            log("Cannot load module on fly: APK not found")
            return
        }
        val pathClassLoader = PathClassLoader(path, ClassLoader.getSystemClassLoader())
        val clazz = Class.forName("$MAGICIAN_PACKAGE_NAME.backend.WechatHook", true, pathClassLoader)
        val method = clazz.getDeclaredMethod("handleLoadWechat", lpparam.javaClass, Context::class.java)
        method.isAccessible = true
        method.invoke(clazz.newInstance(), lpparam, context)
    }
}
