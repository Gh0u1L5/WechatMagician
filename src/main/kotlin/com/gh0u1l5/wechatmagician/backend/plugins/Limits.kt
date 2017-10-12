package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.TextView
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.backend.WechatEvents
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.WechatResHook
import com.gh0u1l5.wechatmagician.storage.HookStatus
import com.gh0u1l5.wechatmagician.storage.LocalizedResources
import com.gh0u1l5.wechatmagician.storage.Preferences
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

class Limits(private val preferences: Preferences) {

    private val pkg = WechatPackage
    private val res = LocalizedResources
    private val events = WechatEvents

    // Hook AlbumPreviewUI to bypass the limit on number of selected photos.
    fun breakSelectPhotosLimit() {
        if (pkg.AlbumPreviewUI == null) {
            return
        }

        XposedHelpers.findAndHookMethod(pkg.AlbumPreviewUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent ?: return
                val current = intent.getIntExtra("max_select_count", 9)
                val limit = try {
                    preferences.getString(
                            "settings_select_photos_limit", "1000"
                    ).toInt()
                } catch (_: Throwable) { 1000 }
                if (current <= 9) {
                    intent.putExtra("max_select_count", current + limit - 9)
                }
            }
        })

        HookStatus += "BreakSelectPhotosLimit"
    }

    fun breakSelectContactLimit() {
        if (pkg.MMActivity == null || pkg.SelectContactUI == null) {
            return
        }

        // Hook MMActivity.onCreateOptionsMenu to add "Select All" button.
        XposedHelpers.findAndHookMethod(pkg.MMActivity, "onCreateOptionsMenu", C.Menu, object : XC_MethodHook() {
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
                    selectAll.title = res["button_select_all"] + "  " +
                            if (checked) "\u2611" else "\u2610"
                    selectAll.setOnMenuItemClickListener {
                        events.onSelectContactUISelectAll(activity, !selectAll.isChecked); true
                    }
                } else {
                    val checkedTextView = activity.layoutInflater.inflate(
                            WechatResHook.MODULE_RES?.getLayout(R.layout.wechat_checked_textview), null
                    )
                    checkedTextView.findViewById<TextView>(R.id.ctvTextView).apply {
                        setTextColor(Color.WHITE)
                        text = res["button_select_all"]
                    }
                    checkedTextView.findViewById<CheckBox>(R.id.ctvCheckBox).apply {
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
        XposedHelpers.findAndHookMethod(pkg.SelectContactUI, "onActivityResult", C.Int, C.Int, C.Intent, object : XC_MethodHook() {
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
        XposedHelpers.findAndHookMethod(pkg.SelectContactUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent ?: return
                if (intent.getIntExtra("max_limit_num", -1) == 9) {
                    intent.putExtra("max_limit_num", 0x7FFFFFFF)
                }
            }
        })

        HookStatus += "BreakSelectContactLimit"
    }

    // Hook SelectConversationUI to bypass the limit on number of recipients.
    fun breakSelectConversationLimit() {
        if (pkg.SelectConversationUI == null || pkg.SelectConversationUIMaxLimitMethod == "") {
            return
        }

        XposedHelpers.findAndHookMethod(pkg.SelectConversationUI, pkg.SelectConversationUIMaxLimitMethod, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = false
            }
        })

        HookStatus += "BreakSelectConversationLimit"
    }
}