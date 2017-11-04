package com.gh0u1l5.wechatmagician.backend.plugins

import android.content.Context
import com.gh0u1l5.wechatmagician.util.FileUtil
import com.gh0u1l5.wechatmagician.util.FileUtil.getApplicationDataDir
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.io.File

class Frontend(private val loader: ClassLoader) {
    fun notifyStatus() {
        findAndHookMethod(
                "com.gh0u1l5.wechatmagician.frontend.fragments.StatusFragment", loader,
                "isModuleLoaded", object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam): Any = true
        })
    }

    fun setDirectoryPermissions(context: Context?) {
        try {
            val dataDir = File(getApplicationDataDir(context))
            FileUtil.setWorldExecutable(dataDir)

            val cacheDir = File(dataDir, "cache")
            cacheDir.mkdir()
            FileUtil.setWorldWritable(cacheDir)
            FileUtil.setWorldExecutable(cacheDir)
        } catch (e: Throwable) {
            log("FRONT => $e")
        }
    }
}