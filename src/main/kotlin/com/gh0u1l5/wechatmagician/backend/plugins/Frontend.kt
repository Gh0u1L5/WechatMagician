package com.gh0u1l5.wechatmagician.backend.plugins

import android.content.Context
import com.gh0u1l5.wechatmagician.Global.FOLDER_SHARED
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.util.FileUtil
import com.gh0u1l5.wechatmagician.util.FileUtil.getApplicationDataDir
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.io.File

object Frontend {

    private var loader: ClassLoader? = null

    @JvmStatic fun init(_loader: ClassLoader) {
        loader = _loader
    }

    @JvmStatic fun notifyStatus() {
        try {
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
        } catch (e: Throwable) {
            log("FRONTEND => $e")
        }
    }

    @JvmStatic fun setDirectoryPermissions(context: Context?) {
        try {
            val dataDir = File(getApplicationDataDir(context))
            FileUtil.setWorldExecutable(dataDir)

            val sharedDir = File(dataDir, FOLDER_SHARED)
            sharedDir.mkdir()
            FileUtil.setWorldWritable(sharedDir)
            FileUtil.setWorldExecutable(sharedDir)
        } catch (e: Throwable) {
            log("FRONTEND => $e")
        }
    }
}