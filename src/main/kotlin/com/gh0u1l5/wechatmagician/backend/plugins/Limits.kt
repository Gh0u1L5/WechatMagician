package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.TextView
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SELECT_PHOTOS_LIMIT
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.backend.WechatEvents
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.WechatResHook
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.BUTTON_SELECT_ALL
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Limits {

    private var preferences: Preferences? = null

    @JvmStatic fun init(_preferences: Preferences) {
        preferences = _preferences
    }

    private val str = LocalizedStrings
    private val pkg = WechatPackage
    private val events = WechatEvents

    // Hook AlbumPreviewUI to bypass the limit on number of selected photos.
    @JvmStatic fun breakSelectPhotosLimit() {
        if (pkg.AlbumPreviewUI == null) {
            return
        }

        findAndHookMethod(
                pkg.AlbumPreviewUI, "onCreate",
                C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent ?: return
                val current = intent.getIntExtra("max_select_count", 9)
                val limit = try {
                    preferences!!.getString(
                            SETTINGS_SELECT_PHOTOS_LIMIT, "1000"
                    ).toInt()
                } catch (_: Throwable) { 1000 }
                if (current <= 9) {
                    intent.putExtra("max_select_count", current + limit - 9)
                }
            }
        })
    }

    @JvmStatic fun breakSelectContactLimit() {
        if (pkg.MMActivity == null || pkg.SelectContactUI == null) {
            return
        }

        // Hook MMActivity.onCreateOptionsMenu to add "Select All" button.
        findAndHookMethod(
                pkg.MMActivity, "onCreateOptionsMenu",
                C.Menu, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject.javaClass != pkg.SelectContactUI) {
                    return
                }

                val menu = param.args[0] as Menu? ?: return
                val activity = param.thisObject as Activity
                val checked = activity.intent?.getBooleanExtra(
                        "select_all_checked", false
                ) ?: false

                val selectAll = menu.add(0, 2, 0, "")
                selectAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                if (WechatResHook.MODULE_RES == null) {
                    selectAll.isChecked = checked
                    selectAll.title = str[BUTTON_SELECT_ALL] + "  " +
                            if (checked) "\u2611" else "\u2610"
                    selectAll.setOnMenuItemClickListener {
                        events.onSelectContactUISelectAll(activity, !selectAll.isChecked); true
                    }
                } else {
                    val layout = WechatResHook.MODULE_RES?.getLayout(R.layout.wechat_checked_textview)
                    val checkedTextView = activity.layoutInflater.inflate(layout, null)
                    checkedTextView.findViewById<TextView>(R.id.ctv_text).apply {
                        setTextColor(Color.WHITE)
                        text = str[BUTTON_SELECT_ALL]
                    }
                    checkedTextView.findViewById<CheckBox>(R.id.ctv_checkbox).apply {
                        isChecked = checked
                        setOnCheckedChangeListener { _, checked ->
                            events.onSelectContactUISelectAll(activity, checked)
                        }
                    }
                    selectAll.actionView = checkedTextView
                }
            }
        })

        // Hook SelectContactUI to help the "Select All" button.
        findAndHookMethod(
                pkg.SelectContactUI, "onActivityResult",
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

        // Hook SelectContactUI to bypass the limit on number of recipients.
        findAndHookMethod(
                pkg.SelectContactUI, "onCreate",
                C.Bundle, object : XC_MethodHook() {
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
    @JvmStatic fun breakSelectConversationLimit() {
        if (pkg.SelectConversationUI == null || pkg.SelectConversationUIMaxLimitMethod == null) {
            return
        }

        findAndHookMethod(pkg.SelectConversationUI, pkg.SelectConversationUIMaxLimitMethod, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = false
            }
        })
    }
}