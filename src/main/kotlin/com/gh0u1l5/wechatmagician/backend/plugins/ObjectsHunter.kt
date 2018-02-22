package com.gh0u1l5.wechatmagician.backend.plugins

import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.backend.WechatPackage.AddressAdapterObject
import com.gh0u1l5.wechatmagician.backend.WechatPackage.ConversationAdapterObject
import com.gh0u1l5.wechatmagician.backend.WechatPackage.MainDatabaseObject
import com.gh0u1l5.wechatmagician.backend.WechatPackage.SnsDatabaseObject
import com.gh0u1l5.wechatmagician.backend.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.backend.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.backend.interfaces.IDatabaseHookRaw
import de.robv.android.xposed.XC_MethodHook
import java.lang.ref.WeakReference

object ObjectsHunter : IActivityHook, IAdapterHook, IDatabaseHookRaw {

    // TODO: hook more objects in this plugin

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