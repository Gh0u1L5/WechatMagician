package com.gh0u1l5.wechatmagician.spellbook.hookers

import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IFileSystemHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.io.File

object FileSystem : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IFileSystemHook::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(C.File, "delete", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val file = param.thisObject as File
                notify("onFileDeleting") { plugin ->
                    val interrupt = (plugin as IFileSystemHook).onFileDeleting(file)
                    if (interrupt) {
                        param.result = true
                    }
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                val file = param.thisObject as File
                notify("onFileDeleted") { plugin ->
                    (plugin as IFileSystemHook).onFileDeleted(file)
                }
            }
        })
        findAndHookConstructor(C.FileInputStream, C.File, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val file = param.args[0] as File
                notify("onFileReading") { plugin ->
                    (plugin as IFileSystemHook).onFileReading(file)
                }
            }
        })
        findAndHookConstructor(C.FileOutputStream, C.File, C.Boolean, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val file = param.args[0] as File
                val overwrite = param.args[1] as Boolean
                notify("onFileWriting") { plugin ->
                    (plugin as IFileSystemHook).onFileWriting(file, overwrite)
                }
            }
        })
        WechatStatus.toggle(WechatStatus.StatusFlag.STATUS_FLAG_FILESYSTEM)
    }
}