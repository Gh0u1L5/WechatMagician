package com.gh0u1l5.wechatmagician.spellbook.hookers

import com.gh0u1l5.wechatmagician.spellbook.WechatPackage
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.hookers.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IFileSystemHookRaw
import com.gh0u1l5.wechatmagician.spellbook.util.C
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object FileSystem : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IFileSystemHookRaw::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookConstructor(
                "java.io.FileOutputStream", WechatPackage.loader,
                C.File, C.Boolean, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeFileWrite") { plugin ->
                    (plugin as IFileSystemHookRaw).beforeFileWrite(param)
                }
            }
        })
        findAndHookMethod("java.io.File", WechatPackage.loader, "delete", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeFileDelete") { plugin ->
                    (plugin as IFileSystemHookRaw).beforeFileDelete(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterFileDelete") { plugin ->
                    (plugin as IFileSystemHookRaw).afterFileDelete(param)
                }
            }
        })
    }
}