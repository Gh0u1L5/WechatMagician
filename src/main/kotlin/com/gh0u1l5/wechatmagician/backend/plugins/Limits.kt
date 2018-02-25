package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.HeaderViewListAdapter
import android.widget.ListView
import android.widget.TextView
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SELECT_PHOTOS_LIMIT
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.BUTTON_SELECT_ALL
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.MMActivity
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SelectContactUI
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SelectConversationUI
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SelectConversationUIMaxLimitMethod
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.lang.reflect.Field

object Limits : IActivityHook {

    private val str = LocalizedStrings
    private val pref = WechatHook.settings

    // Hook AlbumPreviewUI to bypass the limit on number of selected photos.
    override fun onAlbumPreviewUICreated(activity: Activity) {
        val intent = activity.intent ?: return
        val current = intent.getIntExtra("max_select_count", 9)
        val limit = try {
            pref.getString(SETTINGS_SELECT_PHOTOS_LIMIT, "1000").toInt()
        } catch (_: Throwable) { 1000 }
        if (current <= 9) {
            intent.putExtra("max_select_count", current + limit - 9)
        }
    }

    @WechatHookMethod @JvmStatic fun breakSelectContactLimit() {
        // Hook MMActivity.onCreateOptionsMenu to add "Select All" button.
        findAndHookMethod(
                MMActivity, "onCreateOptionsMenu",
                Menu::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject::class.java != SelectContactUI) {
                    return
                }

                val menu = param.args[0] as Menu? ?: return
                val activity = param.thisObject as Activity
                val checked = activity.intent?.getBooleanExtra(
                        "select_all_checked", false
                ) ?: false

                val selectAll = menu.add(0, 2, 0, "")
                selectAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                if (WechatHook.resources == null) {
                    selectAll.isChecked = checked
                    selectAll.title = str[BUTTON_SELECT_ALL] + "  " +
                            if (checked) "\u2611" else "\u2610"
                    selectAll.setOnMenuItemClickListener {
                        onSelectContactUISelectAll(activity, !selectAll.isChecked); true
                    }
                } else {
                    val layout = WechatHook.resources?.getLayout(R.layout.wechat_checked_textview)
                    val checkedTextView = activity.layoutInflater.inflate(layout, null)
                    checkedTextView.findViewById<TextView>(R.id.ctv_text).apply {
                        setTextColor(Color.WHITE)
                        text = str[BUTTON_SELECT_ALL]
                    }
                    checkedTextView.findViewById<CheckBox>(R.id.ctv_checkbox).apply {
                        isChecked = checked
                        setOnCheckedChangeListener { _, checked ->
                            onSelectContactUISelectAll(activity, checked)
                        }
                    }
                    selectAll.actionView = checkedTextView
                }
            }
        })

        // Hook SelectContactUI to help the "Select All" button.
        findAndHookMethod(
                SelectContactUI, "onActivityResult",
                Int::class.java, Int::class.java, Intent::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val requestCode = param.args[0] as Int
                val resultCode = param.args[1] as Int
                val data = param.args[2] as Intent?

                if (requestCode == 5) {
                    val activity = param.thisObject as Activity
                    activity.setResult(resultCode, data)
                    activity.finish()
                    param.result = null
                }
            }
        })

        // Hook SelectContactUI to bypass the limit on number of recipients.
        findAndHookMethod(
                SelectContactUI, "onCreate",
                Bundle::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent ?: return
                if (intent.getIntExtra("max_limit_num", -1) == 9) {
                    intent.putExtra("max_limit_num", 0x7FFFFFFF)
                }
            }
        })
    }

    // Hook SelectConversationUI to bypass the limit on number of recipients.
    @WechatHookMethod @JvmStatic fun breakSelectConversationLimit() {
        findAndHookMethod(SelectConversationUI, SelectConversationUIMaxLimitMethod, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = false
            }
        })
    }

    // Handle the logic about "select all" check box in SelectContactUI
    private fun onSelectContactUISelectAll(activity: Activity, isChecked: Boolean) {
        val intent = activity.intent ?: return
        intent.putExtra("select_all_checked", isChecked)
        intent.putExtra("already_select_contact", "")
        if (isChecked) {
            // Search for the ListView of contacts
            val listView = XposedHelpers.findFirstFieldByExactType(activity::class.java, ListView::class.java)
                    .get(activity) as ListView? ?: return
            val adapter = (listView.adapter as HeaderViewListAdapter).wrappedAdapter

            // Construct the list of user names
            var contactField: Field? = null
            var usernameField: Field? = null
            val userList = mutableListOf<String>()
            repeat(adapter.count, next@ { index ->
                val item = adapter.getItem(index)

                if (contactField == null) {
                    contactField = item::class.java.fields.firstOrNull {
                        it.type.name == WechatPackage.ContactInfoClass.name
                    } ?: return@next
                }
                val contact = contactField?.get(item) ?: return@next

                if (usernameField == null) {
                    usernameField = contact::class.java.fields.firstOrNull {
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
}