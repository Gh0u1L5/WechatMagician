package com.gh0u1l5.wechatmagician.backend.foundation

import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_IMG_STORAGE
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_MSG_STORAGE
import com.gh0u1l5.wechatmagician.Global.tryAsynchronously
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.cache.MessageCache
import com.gh0u1l5.wechatmagician.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.getLongField
import java.io.File

object Storage {

    private val pkg = WechatPackage

    @JvmStatic fun hookMsgStorage() {
        // Analyze dynamically to find the global message storage instance.
        hookAllConstructors(pkg.MsgStorageClass, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (pkg.MsgStorageObject !== param.thisObject) {
                    pkg.MsgStorageObject = param.thisObject
                }
            }
        })

        // Hook MsgStorage to record the received messages.
        findAndHookMethod(pkg.MsgStorageClass, pkg.MsgStorageInsertMethod, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                tryAsynchronously {
                    val msg = param.args[0]
                    val msgId = getLongField(msg, "field_msgId")
                    MessageCache[msgId] = msg
                }
            }
        })

        pkg.setStatus(STATUS_FLAG_MSG_STORAGE, true)
    }

    @JvmStatic fun hookImgStorage() {
        // Analyze dynamically to find the global image storage instance.
        hookAllConstructors(pkg.ImgStorageClass, object : XC_MethodHook() {
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

//        // Hook FileOutputStream to prevent Wechat from overwriting disk cache.
//        findAndHookConstructor(
//                "java.io.FileOutputStream", loader,
//                C.File, C.Boolean, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val path = (param.args[0] as File?)?.absolutePath ?: return
//                if (path in ImageUtil.blockTable) {
//                    param.throwable = IOException()
//                }
//            }
//        })

        pkg.setStatus(STATUS_FLAG_IMG_STORAGE, true)
    }

    @JvmStatic fun hookFileStorage() {
        findAndHookMethod("java.io.File", pkg.loader, "delete", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val path = (param.thisObject as File).absolutePath
                when {
                    path.contains("/image2/") -> param.result = true
                    path.contains("/video/")  -> param.result = true
                }
            }
        })
    }
}
