package com.gh0u1l5.wechatmagician.xposed

import android.app.Activity
import android.content.Intent
import android.view.MenuItem
import android.widget.HeaderViewListAdapter
import android.widget.ListView
import com.gh0u1l5.wechatmagician.util.C
import de.robv.android.xposed.XposedHelpers.*
import java.lang.reflect.Field

object WechatButtons {

    data class WechatButton (
            val groupId: Int = 0,
            val itemId: Int,
            val order: Int = 0,
            val title: String,

            val decorate: (MenuItem, Any) -> Unit,
            val listener: (Any) -> MenuItem.OnMenuItemClickListener
    )

    private val pkg = WechatPackage
    private val registries: MutableMap<String, WechatButton> = mutableMapOf()

    init {
        registries["com.tencent.mm.ui.contact.SelectContactUI"] = WechatButton(
            itemId = 2, title = ModuleResources.buttonSelectAll,
            decorate = { item, thisObject ->
                val intent = (thisObject as Activity).intent
                item.isCheckable = true
                item.isChecked = intent.getBooleanExtra("select_all_checked", false)
            },
            listener = { thisObject ->
                val activity = thisObject as Activity
                MenuItem.OnMenuItemClickListener { menuItem ->
                    if (pkg.ContactInfoClass == "") {
                        return@OnMenuItemClickListener false
                    }

                    if (menuItem.isChecked) {
                        // Invoke new SelectContactUI without any selected contacts.
                        val intent = callMethod(
                                activity, "getIntent"
                        ) as Intent? ?: return@OnMenuItemClickListener false
                        intent.putExtra("already_select_contact", "")
                        intent.putExtra("select_all_checked", !menuItem.isChecked)
                        activity.startActivityForResult(intent, 5)
                    } else {
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

                        // Invoke new SelectContactUI with all contacts selected
                        val intent = callMethod(
                                activity, "getIntent"
                        ) as Intent? ?: return@OnMenuItemClickListener false
                        intent.putExtra("already_select_contact", userList.joinToString(","))
                        intent.putExtra("select_all_checked", !menuItem.isChecked)
                        activity.startActivityForResult(intent, 5)
                    }

                    return@OnMenuItemClickListener true
                }
            })
    }

    operator fun get(UIClassName: String): WechatButton? {
        return registries[UIClassName]
    }
}
