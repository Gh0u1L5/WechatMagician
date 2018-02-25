package com.gh0u1l5.wechatmagician.spellbook.hookers

import com.gh0u1l5.wechatmagician.spellbook.Global.STATUS_FLAG_IMG_STORAGE
import com.gh0u1l5.wechatmagician.spellbook.Global.STATUS_FLAG_MSG_STORAGE
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.ImgStorageClass
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.ImgStorageLoadMethod
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.MsgStorageClass
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.MsgStorageInsertMethod
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.hookers.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IImageStorageHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IMessageStorageHook
import com.gh0u1l5.wechatmagician.spellbook.util.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.getLongField

object Storage : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IMessageStorageHook::class.java, IImageStorageHook::class.java)

    @WechatHookMethod @JvmStatic fun hookMessageStorage() {
        // Analyze dynamically to find the global message storage instance.
        hookAllConstructors(MsgStorageClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onMessageStorageCreated") { plugin ->
                    (plugin as IMessageStorageHook).onMessageStorageCreated(param.thisObject)
                }
            }
        })

        // Hook MsgStorage to record the received messages.
        findAndHookMethod(MsgStorageClass, MsgStorageInsertMethod, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val msgObject = param.args[0]
                val msgId = getLongField(msgObject, "field_msgId")
                notify("onMessageStorageInserted") { plugin ->
                    (plugin as IMessageStorageHook).onMessageStorageInserted(msgId, msgObject)
                }
            }
        })

        WechatStatus.toggle(STATUS_FLAG_MSG_STORAGE, true)
    }

    @WechatHookMethod @JvmStatic fun hookImageStorage() {
        // Analyze dynamically to find the global image storage instance.
        hookAllConstructors(ImgStorageClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onImageStorageCreated") { plugin ->
                    (plugin as IImageStorageHook).onImageStorageCreated(param.thisObject)
                }
            }
        })

        findAndHookMethod(
                ImgStorageClass, ImgStorageLoadMethod,
                C.String, C.String, C.String, C.Boolean, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val imageId = param.args[0] as String?
                val prefix = param.args[1] as String?
                val suffix = param.args[2] as String?
                notify("onImageStorageLoaded") { plugin ->
                    (plugin as IImageStorageHook).onImageStorageLoaded(imageId, prefix, suffix)
                }
            }
        })

        WechatStatus.toggle(STATUS_FLAG_IMG_STORAGE, true)
    }
}
