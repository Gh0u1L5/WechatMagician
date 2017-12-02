package com.gh0u1l5.wechatmagician.backend

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.*
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.frontend.WechatListPopupAdapter
import com.gh0u1l5.wechatmagician.storage.Strings
import com.gh0u1l5.wechatmagician.util.FileUtil
import com.gh0u1l5.wechatmagician.util.ImageUtil
import com.gh0u1l5.wechatmagician.util.ViewUtil
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px
import de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType
import java.lang.reflect.Field

object WechatEvents {

    private val str = Strings
    private val pkg = WechatPackage

    // Handle the logic about "select all" check box in SelectContactUI
    fun onSelectContactUISelectAll(activity: Activity, isChecked: Boolean) {
        if (pkg.ContactInfoClass == null) {
            return
        }

        val intent = activity.intent ?: return
        intent.putExtra("select_all_checked", isChecked)
        intent.putExtra("already_select_contact", "")
        if (isChecked) {
            // Search for the ListView of contacts
            val listView = findFirstFieldByExactType(activity.javaClass, C.ListView)
                    .get(activity) as ListView? ?: return
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
            intent.putExtra("already_select_contact", userList.joinToString(","))
        }
        activity.startActivityForResult(intent, 5)
    }

    // Show a popup menu in SnsTimelineUI
    fun onTimelineItemLongClick(parent: AdapterView<*>, view: View, snsId: Long): Boolean {
        val operations = listOf(str["menu_sns_forward"], str["menu_sns_screenshot"])
        ListPopupWindow(parent.context).apply {
            width = parent.context.dp2px(100)
            anchorView = view
            setDropDownGravity(Gravity.CENTER)
            setAdapter(WechatListPopupAdapter(view.context, operations))
            setOnItemClickListener { _, _, operation, _ ->
                onTimelineItemPopupMenuSelected(view, snsId, operation)
                dismiss()
            }
        }.show()
        return true
    }

    // Handle the logic about the popup menu in SnsTimelineUI
    private fun onTimelineItemPopupMenuSelected(itemView: View, snsId: Long, operation: Int): Boolean {
        when (operation) {
            0 -> { // Forward
                ForwardAsyncTask(snsId, itemView.context).execute()
                Toast.makeText(
                        itemView.context, str["prompt_wait"], Toast.LENGTH_SHORT
                ).show()
                return true
            }
            1 -> { // Screenshot
                val path = ImageUtil.createScreenshotPath()
                val bitmap = ViewUtil.drawView(itemView)
                FileUtil.writeBitmapToDisk(path, bitmap)
                FileUtil.notifyNewMediaFile(path, itemView.context)
                Toast.makeText(
                        itemView.context, str["prompt_screenshot"] + path, Toast.LENGTH_SHORT
                ).show()
                return true
            }
            else -> return false
        }
    }
}
