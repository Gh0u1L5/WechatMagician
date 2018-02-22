package com.gh0u1l5.wechatmagician.backend.foundation

import android.app.Activity
import android.content.Context
import android.view.ContextMenu
import android.view.View
import android.widget.AdapterView
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.foundation.base.EventCenter
import com.gh0u1l5.wechatmagician.backend.interfaces.IPopupMenuHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.*

object MenuAppender : EventCenter() {

    data class PopupMenuItem (
            val groupId: Int,
            val itemId: Int,
            val order: Int,
            val title: String,
            val onClickListener: (context: Context) -> Unit
    )

    private val pkg = WechatPackage

    @Volatile var currentUsername: String? = null
    @Volatile var currentMenuItems: List<PopupMenuItem>? = null

    @JvmStatic fun hijackPopupMenuEvents() {
        findAndHookMethod(pkg.MMListPopupWindow, "show", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val listenerField = findFirstFieldByExactType(pkg.MMListPopupWindow, C.AdapterView_OnItemClickListener)
                val listener = listenerField.get(param.thisObject) as AdapterView.OnItemClickListener
                listenerField.set(param.thisObject, AdapterView.OnItemClickListener { parent, view, position, id ->
                    val title = parent.adapter.getItem(position)
                    val context = getObjectField(param.thisObject, "mContext") as Context
                    currentMenuItems?.forEach {
                        if (title == it.title) {
                            it.onClickListener(context)
                        }
                    }
                    listener.onItemClick(parent, view, position, id)
                })
            }
        })
        findAndHookMethod(pkg.MMListPopupWindow, "dismiss", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                currentUsername = null
                currentMenuItems = null
            }
        })
    }

    @JvmStatic fun hijackPopupMenuForContacts() {
        findAndHookMethod(
                pkg.ContactLongClickListener, "onItemLongClick",
                C.AdapterView, C.View, C.Int, C.Long, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val parent = param.args[0] as AdapterView<*>
                val position = param.args[2] as Int
                val item = parent.adapter?.getItem(position)
                currentUsername = getObjectField(item, "field_username") as String?
            }
        })

        findAndHookMethod(
                pkg.AddressUI, "onCreateContextMenu",
                C.ContextMenu, C.View, C.ContextMenuInfo, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val menu = param.args[0] as ContextMenu
                val view = param.args[1] as View

                currentMenuItems = notifyForResult("onCreatePopupMenuForContacts") { plugin ->
                    return@notifyForResult if (plugin is IPopupMenuHook) {
                        plugin.onCreatePopupMenuForContacts(currentUsername ?: "")
                    } else null
                }.sortedBy { it.itemId }

                currentMenuItems?.forEach {
                    val item = menu.add(it.groupId, it.itemId, it.order, it.title)
                    item.setOnMenuItemClickListener { _ ->
                        it.onClickListener(view.context) // TODO: can we use param.thisObject here?
                        return@setOnMenuItemClickListener true
                    }
                }
            }
        })


    }

    @JvmStatic fun hijackPopupMenuForConversations() {
        findAndHookMethod(
                pkg.ConversationLongClickListener, "onItemLongClick",
                C.AdapterView, C.View, C.Int, C.Long, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val parent = param.args[0] as AdapterView<*>
                val position = param.args[2] as Int
                val item = parent.adapter?.getItem(position)
                currentUsername = getObjectField(item, "field_username") as String?
            }
        })

        findAndHookMethod(
                pkg.ConversationCreateContextMenuListener, "onCreateContextMenu",
                C.ContextMenu, C.View, C.ContextMenuInfo, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val menu = param.args[0] as ContextMenu

                currentMenuItems = notifyForResult("onCreatePopupMenuForConversations") { plugin ->
                    return@notifyForResult if (plugin is IPopupMenuHook) {
                        plugin.onCreatePopupMenuForConversations(currentUsername ?: "")
                    } else null
                }.sortedBy { it.itemId }

                currentMenuItems?.forEach {
                    val item = menu.add(it.groupId, it.itemId, it.order, it.title)
                    item.setOnMenuItemClickListener { _ ->
                        it.onClickListener(param.thisObject as Activity)
                        return@setOnMenuItemClickListener true
                    }
                }
            }
        })
    }
}