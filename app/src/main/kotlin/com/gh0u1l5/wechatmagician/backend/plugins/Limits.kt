package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SELECT_PHOTOS_LIMIT
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.storage.Strings
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.base.Hooker
import com.gh0u1l5.wechatmagician.spellbook.base.HookerProvider
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.plugin.gallery.ui.Classes.AlbumPreviewUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.storage.Classes.ContactInfo
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.ui.contact.Classes.SelectContactUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.ui.transmit.Methods.SelectConversationUI_checkLimit
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.lang.reflect.Field

object Limits : IActivityHook, HookerProvider {

    private val pref = WechatHook.settings

    override fun provideStaticHookers(): List<Hooker>? {
        return listOf(onCheckSelectLimitHooker, onSelectAllContactHooker)
    }

    override fun onActivityCreating(activity: Activity, savedInstanceState: Bundle?) {
        when (activity::class.java) {
            AlbumPreviewUI -> {
                // Bypass the limit on number of photos the user can select
                val intent = activity.intent ?: return
                val oldLimit = intent.getIntExtra("max_select_count", 9)
                val newLimit = try {
                    pref.getString(SETTINGS_SELECT_PHOTOS_LIMIT, "1000").toInt()
                } catch (_: Throwable) { 1000 }
                if (oldLimit <= 9) {
                    intent.putExtra("max_select_count", oldLimit + newLimit - 9)
                }
            }
            SelectContactUI -> {
                // Bypass the limit on number of recipients the user can forward.
                val intent = activity.intent ?: return
                if (intent.getIntExtra("max_limit_num", -1) == 9) {
                    intent.putExtra("max_limit_num", 0x7FFFFFFF)
                }
            }
        }
    }

    // Hook MMActivity.onCreateOptionsMenu to add "Select All" button.
    override fun onMMActivityOptionsMenuCreated(activity: Activity, menu: Menu) {
        if (activity::class.java != SelectContactUI) {
            return
        }

        val intent = activity.intent ?: return
        val checked = intent.getBooleanExtra("select_all_checked", false)

        val selectAll = menu.add(0, 2, 0, "")
        selectAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        val btnText = TextView(activity).apply {
            setTextColor(Color.WHITE)
            text = Strings.getString(R.string.button_select_all)
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = context.dp2px(4)
            }
        }
        val btnCheckbox = CheckBox(activity).apply {
            isChecked = checked
            setOnCheckedChangeListener { _, checked ->
                onSelectContactUISelectAll(activity, checked)
            }
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = context.dp2px(6)
            }
        }
        selectAll.actionView = LinearLayout(activity).apply {
            addView(btnText)
            addView(btnCheckbox)
            orientation = LinearLayout.HORIZONTAL
        }
    }

    // Hook SelectConversationUI to bypass the limit on number of recipients.
    private val onCheckSelectLimitHooker = Hooker {
        hookMethod(SelectConversationUI_checkLimit, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = false
            }
        })
    }

    // Hook SelectContactUI to help the "Select All" button.
    private val onSelectAllContactHooker = Hooker {
        findAndHookMethod(
                SelectContactUI, "onActivityResult",
                C.Int, C.Int, C.Intent, object : XC_MethodHook() {
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
                        it.type.name == ContactInfo.name
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