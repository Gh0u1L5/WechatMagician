package com.gh0u1l5.wechatmagician.backend.plugins

import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.HookStatus
import com.gh0u1l5.wechatmagician.storage.MessageCache
import com.gh0u1l5.wechatmagician.util.ImageUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

class Storage(private val loader: ClassLoader) {

    private val pkg = WechatPackage

    fun hookMsgStorage() {
        if (pkg.MsgStorageClass == null || pkg.MsgStorageInsertMethod == "") {
            return
        }

        // Analyze dynamically to find the global message storage instance.
        XposedBridge.hookAllConstructors(pkg.MsgStorageClass, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (pkg.MsgStorageObject !== param.thisObject) {
                    pkg.MsgStorageObject = param.thisObject
                }
            }
        })

        // Hook MsgStorage to record the received messages.
        XposedHelpers.findAndHookMethod(pkg.MsgStorageClass, pkg.MsgStorageInsertMethod, pkg.MsgInfoClass, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                thread(start = true) {
                    val msg = param.args[0]
                    val msgId = XposedHelpers.getLongField(msg, "field_msgId")
                    MessageCache[msgId] = msg
                }
            }
        })

        HookStatus.MsgStorage = true
    }

    fun hookImgStorage() {
        if (pkg.ImgStorageClass == null) {
            return
        }

        // Analyze dynamically to find the global image storage instance.
        XposedBridge.hookAllConstructors(pkg.ImgStorageClass, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (pkg.ImgStorageObject !== param.thisObject) {
                    pkg.ImgStorageObject = param.thisObject
                }
            }
        })

//        findAndHookMethod(pkg.ImgStorageClass, pkg.ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun afterHookedMethod(param: MethodHookParam) {
//                val imgId = param.args[0] as String?
//                val prefix = param.args[1] as String?
//                val suffix = param.args[2] as String?
//                log("IMG => imgId = $imgId, prefix = $prefix, suffix = $suffix")
//            }
//        })

        // Hook FileOutputStream to prevent Wechat from overwriting disk cache.
        XposedHelpers.findAndHookConstructor("java.io.FileOutputStream", loader, C.File, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val path = (param.args[0] as File?)?.path ?: return
                if (path in ImageUtil.blockTable) {
                    param.throwable = IOException()
                }
            }
        })

        HookStatus.ImgStorage = true
    }
}