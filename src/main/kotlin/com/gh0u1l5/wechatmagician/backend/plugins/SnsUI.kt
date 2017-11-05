package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatEvents
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType
import de.robv.android.xposed.XposedHelpers.getLongField

object SnsUI {

    private val pkg = WechatPackage
    private val events = WechatEvents

    // Hook SnsUserUI.onCreate to popup a menu during long click.
    @JvmStatic fun setLongClickListenerForSnsUserUI() {
        if (pkg.SnsUserUI == null) {
            return
        }

        XposedHelpers.findAndHookMethod(pkg.SnsUserUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                registerSnsPopupWindow(param.thisObject)
            }
        })
    }

    // Hook SnsTimeLineUI.onCreate to popup a menu during long click.
    @JvmStatic fun setLongClickListenerForSnsTimeLineUI() {
        if (pkg.SnsTimeLineUI == null) {
            return
        }

        XposedHelpers.findAndHookMethod(pkg.SnsTimeLineUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                registerSnsPopupWindow(param.thisObject)
            }
        })
    }

    @JvmStatic fun registerSnsPopupWindow(wrapper: Any?) {
        if (pkg.SnsActivity == null || wrapper == null) {
            return
        }

        val activityField = findFirstFieldByExactType(wrapper.javaClass, pkg.SnsActivity)
        val activity = activityField.get(wrapper)
        val listViewField = findFirstFieldByExactType(activity.javaClass, C.ListView)
        val listView = listViewField.get(activity) as ListView

        // Set onLongClickListener for items
        listView.setOnItemLongClickListener { parent, view, position, _ ->
            val item = parent.getItemAtPosition(position)
            val snsId = getLongField(item, "field_snsId")
            events.onTimelineItemLongClick(parent, view, snsId)
        }

        // Hook adapter to make sure the items are long clickable.
        val adapter = listView.adapter ?: return
        XposedHelpers.findAndHookMethod(
                adapter.javaClass, "getView",
                C.Int, C.View, C.ViewGroup, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject === adapter) {
                    val convertView = param.args[1] as View?
                    if (convertView == null) { // this is a new view
                        val view = param.result as View? ?: return
                        if (view is ViewGroup) {
                            repeat(view.childCount, {
                                view.getChildAt(it).isClickable = false
                            })
                        }
                        view.isLongClickable = true
                    }
                }
            }
        })
    }

    // Hook SnsUploadUI.onCreate to clean EditText properly before forwarding.
    @JvmStatic fun cleanTextViewBeforeForwarding() {
        if (pkg.SnsUploadUI == null || pkg.SnsUploadUIEditTextField == "") {
            return
        }

        XposedHelpers.findAndHookMethod(pkg.SnsUploadUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent ?: return
                if (intent.getBooleanExtra("Ksnsforward", false)) {
                    val content = intent.getStringExtra("Kdescription")
                    val editText = XposedHelpers.getObjectField(
                            param.thisObject, pkg.SnsUploadUIEditTextField
                    )
                    XposedHelpers.callMethod(editText, "setText", content)
                }
            }
        })
    }
}