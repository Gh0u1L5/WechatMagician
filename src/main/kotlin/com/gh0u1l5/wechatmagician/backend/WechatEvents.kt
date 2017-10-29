package com.gh0u1l5.wechatmagician.backend

import android.app.Activity
import android.os.Environment
import android.view.Gravity
import android.widget.*
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.frontend.WechatListPopupAdapter
import com.gh0u1l5.wechatmagician.storage.SnsDatabase
import com.gh0u1l5.wechatmagician.storage.Strings
import com.gh0u1l5.wechatmagician.util.FileUtil
import com.gh0u1l5.wechatmagician.util.ViewUtil
import de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*

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
    fun onAdFrameLongClick(layout: FrameLayout, x: Float?, y: Float?): Boolean {
        if (x == null || y == null) {
            return false
        }

        val popup = ListPopupWindow(layout.context)
        popup.width = 320
        popup.anchorView = layout

        val location = IntArray(2)
        layout.getLocationOnScreen(location)
        popup.horizontalOffset = (x - location[0] - layout.width).toInt()
        popup.verticalOffset = (y - location[1] - layout.height).toInt()

        popup.setDropDownGravity(Gravity.BOTTOM or Gravity.END)
        popup.setAdapter(WechatListPopupAdapter(
                layout.context, listOf(str["menu_sns_forward"], str["menu_sns_screenshot"])
        ))
        popup.setOnItemClickListener { _, _, itemId, _ ->
            onAdFramePopupMenuSelected(layout, itemId)
            popup.dismiss()
        }

        popup.show()
        return true
    }

    // Handle the logic about the popup menu in SnsTimelineUI
    private fun onAdFramePopupMenuSelected(layout: FrameLayout, itemId: Int): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault())
        val storage = Environment.getExternalStorageDirectory().absolutePath + "/WechatMagician"
        when (itemId) {
            0 -> {
                if (pkg.PLTextView == null) {
                    return false
                }
                val textView = ViewUtil.searchViewGroup(layout, pkg.PLTextView!!.name)
                val rowId = textView?.tag as String?
                val snsId = SnsDatabase.getSnsId(rowId?.drop("sns_table_".length))
                ForwardAsyncTask(snsId, layout.context).execute()
                Toast.makeText(
                        layout.context, str["prompt_wait"], Toast.LENGTH_SHORT
                ).show()
                return true
            }
            1 -> {
                val time = Calendar.getInstance().time
                val filename = "SNS-${formatter.format(time)}.jpg"
                val path = "$storage/screenshot/$filename"
                val bitmap = ViewUtil.drawView(layout)
                FileUtil.writeBitmapToDisk(path, bitmap)
                FileUtil.galleryAddPic(path, layout.context)
                Toast.makeText(
                        layout.context, str["prompt_screenshot"] + path, Toast.LENGTH_SHORT
                ).show()
                return true
            }
            else -> return false
        }
    }
}
