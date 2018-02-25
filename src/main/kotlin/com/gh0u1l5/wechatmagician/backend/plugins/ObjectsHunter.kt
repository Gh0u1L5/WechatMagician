package com.gh0u1l5.wechatmagician.backend.plugins

import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.AddressAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.ConversationAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.ImgStorageObject
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.MainDatabaseObject
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.MsgStorageObject
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SnsDatabaseObject
import com.gh0u1l5.wechatmagician.spellbook.interfaces.*
import de.robv.android.xposed.XC_MethodHook
import java.lang.ref.WeakReference

object ObjectsHunter : IActivityHook, IAdapterHook, IDatabaseHookRaw, IMessageStorageHook, IImageStorageHook {

    // TODO: hook more objects in this plugin

    override fun onMessageStorageCreated(storage: Any) {
        if (MsgStorageObject !== storage) {
            MsgStorageObject = storage
        }
    }

    override fun onImageStorageCreated(storage: Any) {
        if (ImgStorageObject !== storage) {
            ImgStorageObject = storage
        }
    }

    override fun onAddressAdapterCreated(adapter: BaseAdapter) {
        AddressAdapterObject = WeakReference(adapter)
    }

    override fun onConversationAdapterCreated(adapter: BaseAdapter) {
        ConversationAdapterObject = WeakReference(adapter)
    }

    override fun afterDatabaseOpen(param: XC_MethodHook.MethodHookParam) {
        val path = param.args[0] as String
        if (path.endsWith("SnsMicroMsg.db")) {
            if (SnsDatabaseObject !== param.result) {
                SnsDatabaseObject = param.result
            }
        }
    }

    override fun afterDatabaseUpdate(param: XC_MethodHook.MethodHookParam) {
        val path = param.thisObject?.toString() ?: ""
        if (path.endsWith("EnMicroMsg.db")) {
            if (MainDatabaseObject !== param.thisObject) {
                MainDatabaseObject = param.thisObject
            }
        }
    }
}