package com.gh0u1l5.wechatmagician.backend.plugins

import com.gh0u1l5.wechatmagician.Global.FOLDER_SHARED
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.tryWithLog
import com.gh0u1l5.wechatmagician.util.FileUtil
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.io.File

object Frontend {

    private var loader: ClassLoader? = null

    @JvmStatic fun init(_loader: ClassLoader) {
        loader = _loader
    }

    @Suppress("DEPRECATION")
    @JvmStatic fun notifyStatus() {
        tryWithLog {
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
    }

    @JvmStatic fun setDirectoryPermissions() {
        tryWithLog {
            val dataDir = File(MAGICIAN_PACKAGE_NAME)
            FileUtil.setWorldExecutable(dataDir)

            val sharedDir = File(dataDir, FOLDER_SHARED)
            sharedDir.mkdir()
            FileUtil.setWorldReadable(sharedDir)
            FileUtil.setWorldWritable(sharedDir)
            FileUtil.setWorldExecutable(sharedDir)
        }
    }
}