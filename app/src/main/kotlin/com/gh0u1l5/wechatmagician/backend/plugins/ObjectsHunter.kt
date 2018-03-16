package com.gh0u1l5.wechatmagician.backend.plugins

import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.AddressAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.ConversationAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.ImgStorageObject
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.MainDatabaseObject
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.MsgStorageObject
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.SnsDatabaseObject
import com.gh0u1l5.wechatmagician.spellbook.interfaces.*
import de.robv.android.xposed.XC_MethodHook
import java.lang.ref.WeakReference

object ObjectsHunter : IActivityHook, IAdapterHook, IDatabaseHook, IMessageStorageHook, IImageStorageHook {

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

    override fun onDatabaseOpened(path: String, database: Any?) {
        if (path.endsWith("SnsMicroMsg.db")) {
            if (SnsDatabaseObject !== database) {
                SnsDatabaseObject = database
            }
        }
    }

    override fun onDatabaseUpdated(param: XC_MethodHook.MethodHookParam) {
        val path = param.thisObject?.toString() ?: ""
        if (path.endsWith("EnMicroMsg.db")) {
            if (MainDatabaseObject !== param.thisObject) {
                MainDatabaseObject = param.thisObject
            }
        }
    }
}