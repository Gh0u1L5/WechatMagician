package com.gh0u1l5.wechatmagician.spellbook.hookers

import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IFileSystemHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object FileSystem : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IFileSystemHook::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookConstructor(
                "java.io.FileOutputStream", WechatGlobal.wxLoader,
                C.File, C.Boolean, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onFileWriting") { plugin ->
                    (plugin as IFileSystemHook).onFileWriting(param)
                }
            }
        })
        findAndHookMethod("java.io.File", WechatGlobal.wxLoader, "delete", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onFileDeleting") { plugin ->
                    (plugin as IFileSystemHook).onFileDeleting(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onFileDeleted") { plugin ->
                    (plugin as IFileSystemHook).onFileDeleted(param)
                }
            }
        })
    }
}