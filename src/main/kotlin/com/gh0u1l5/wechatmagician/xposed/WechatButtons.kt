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
            decorate = { menuItem, thisObject ->
                val intent = (thisObject as Activity).intent
                menuItem.isChecked = intent.getBooleanExtra("select_all_checked", false)
                if (menuItem.isChecked) {
                    menuItem.title = ModuleResources.buttonSelectAll + "  \u2611"
                } else {
                    menuItem.title = ModuleResources.buttonSelectAll + "  \u2610"
                }
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            },
            listener = { thisObject ->
                val activity = thisObject as Activity
                MenuItem.OnMenuItemClickListener listener@ { menuItem ->
                    if (pkg.ContactInfoClass == "") {
                        return@listener false
                    }

                    if (menuItem.isChecked) {
                        // Invoke new SelectContactUI without any selected contacts.
                        val intent = callMethod(
                                activity, "getIntent"
                        ) as Intent? ?: return@listener false
                        intent.putExtra("already_select_contact", "")
                        intent.putExtra("select_all_checked", !menuItem.isChecked)
                        activity.startActivityForResult(intent, 5)
                    } else {
                        // Search for the ListView of contacts
                        val listView = findFirstFieldByExactType(activity.javaClass, C.ListView)
                                .get(activity) as ListView? ?: return@listener false
                        val adapter = (listView.adapter as HeaderViewListAdapter).wrappedAdapter

                        // Construct the list of user names
                        var contactField: Field? = null
                        var usernameField: Field? = null
                        val userList = mutableListOf<String>()
                        repeat(adapter.count, next@ { index ->
                            val item = adapter.getItem(index)

                            if (contactField == null) {
                                contactField = item.javaClass.fields.firstOrNull {
                                    it.type.name == pkg.ContactInfoClass
                                } ?: return@next
                            }
                            val contact = contactField?.get(item) ?: return@next

                            if (usernameField == null) {
                                usernameField = contact.javaClass.fields.firstOrNull {
                                    it.name == "field_username"
                                } ?: return@next
                            }
                            val username = usernameField?.get(contact) ?: return@next
                            userList.add(username as String)
                        })

                        // Invoke new SelectContactUI with all contacts selected
                        val intent = callMethod(
                                activity, "getIntent"
                        ) as Intent? ?: return@listener false
                        intent.putExtra("already_select_contact", userList.joinToString(","))
                        intent.putExtra("select_all_checked", !menuItem.isChecked)
                        activity.startActivityForResult(intent, 5)
                    }

                    return@listener true
                }
            })
    }

    operator fun get(UIClassName: String): WechatButton? {
        return registries[UIClassName]
    }
}
