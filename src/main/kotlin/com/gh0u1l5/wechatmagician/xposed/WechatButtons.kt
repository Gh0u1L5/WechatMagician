package com.gh0u1l5.wechatmagician.xposed

import android.app.Activity
import android.content.Intent
import android.view.MenuItem
import android.widget.HeaderViewListAdapter
import android.widget.ListView
import com.gh0u1l5.wechatmagician.util.C
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedHelpers.*
import java.lang.reflect.Field

object WechatButtons {

    data class WechatButton (
            val groupId: Int = 0,
            val itemId: Int,
            val order: Int = 0,
            val title: String,
            val listener: (MethodHookParam) -> MenuItem.OnMenuItemClickListener
    )

    private val pkg = WechatPackage
    private val registries: MutableMap<String, WechatButton> = mutableMapOf()

    init {
        registries["com.tencent.mm.ui.contact.SelectContactUI"] = WechatButton(
            itemId = 2, title = ModuleResources.buttonSelectAll,
            listener = { param ->
                val activity = param.thisObject as Activity
                MenuItem.OnMenuItemClickListener {
                    if (pkg.ContactInfoClass == "") {
                        return@OnMenuItemClickListener false
                    }

                    // Search for the ListView of contacts
                    val listViewField = findFirstFieldByExactType(activity.javaClass, C.ListView)
                    val listView = getObjectField(
                            activity, listViewField.name
                    ) as ListView? ?: return@OnMenuItemClickListener false
                    val adapter = (listView.adapter as HeaderViewListAdapter).wrappedAdapter

                    // Construct the list of user names
                    var contactField: Field? = null
                    val userList = mutableListOf<String>()
                    repeat(adapter.count, next@ { index ->
                        val item = adapter.getItem(index)
                        if (contactField == null) {
                            contactField = item.javaClass.fields.firstOrNull {
                                it.type.name == pkg.ContactInfoClass
                            } ?: return@next
                        }
                        val contact = getObjectField(item, contactField!!.name) ?: return@next
                        userList.add(getObjectField(contact, "field_username") as String)
                    })

                    // Invoke another SelectContactUI
                    val intent = callMethod(
                            activity, "getIntent"
                    ) as Intent? ?: return@OnMenuItemClickListener false
                    intent.putExtra("already_select_contact", userList.joinToString(","))
                    activity.startActivityForResult(intent, 5)

                    return@OnMenuItemClickListener true
                }
            })
    }

    operator fun get(UIClassName: String): WechatButton? {
        return registries[UIClassName]
    }
}
