package com.gh0u1l5.wechatmagician.backend

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.storage.LocalizedResources
import com.gh0u1l5.wechatmagician.storage.SnsCache
import com.gh0u1l5.wechatmagician.util.ImageUtil
import com.gh0u1l5.wechatmagician.util.ViewUtil
import de.robv.android.xposed.XposedHelpers.*
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*

object WechatListeners {

    private val pkg = WechatPackage
    private val res = LocalizedResources

    fun onSelectContactUISelectAllListener(thisObject: Any): MenuItem.OnMenuItemClickListener {
        val activity = thisObject as Activity
        return MenuItem.OnMenuItemClickListener listener@ { menuItem ->
            if (pkg.ContactInfoClass == null) {
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
                            it.type.name == pkg.ContactInfoClass?.name
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
    }

    fun onAdFrameLongClickListener(thisObject: Any): View.OnLongClickListener {
        val layout = thisObject as FrameLayout
        val formatter = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault())
        return View.OnLongClickListener {
            val storage = Environment.getExternalStorageDirectory().path + "/WechatMagician"
            val popup = PopupMenu(layout.context, layout, Gravity.CENTER)
            popup.menu.add(0, 1, 0, res["menu_sns_forward"])
            popup.menu.add(0, 2, 0, res["menu_sns_screenshot"])
            popup.setOnMenuItemClickListener listener@ { item ->
                when (item.itemId) {
                    1 -> {
                        if (pkg.PLTextView == null) {
                            return@listener false
                        }
                        val textView = ViewUtil.searchViewGroup(layout, pkg.PLTextView!!.name)
                        val rowId = textView?.tag as String?
                        val snsId = SnsCache.getSnsId(rowId?.drop("sns_table_".length))
                        val snsInfo = SnsCache[snsId] ?: return@listener false
                        ForwardAsyncTask(snsInfo, layout.context).execute()
                        Toast.makeText(
                                layout.context, res["prompt_wait"], Toast.LENGTH_SHORT
                        ).show()
                        return@listener true
                    }
                    2 -> {
                        val time = Calendar.getInstance().time
                        val filename = "SNS-${formatter.format(time)}.jpg"
                        val path = "$storage/screenshot/$filename"
                        val bitmap = ImageUtil.drawView(layout)
                        ImageUtil.writeBitmapToDisk(path, bitmap)
                        Toast.makeText(
                                layout.context, res["prompt_screenshot"] + path, Toast.LENGTH_SHORT
                        ).show()
                        return@listener true
                    }
                    else -> false
                }
            }
            popup.show(); true
        }
    }
}
