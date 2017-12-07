package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatEvents
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.frontend.wechat.ListPopupPosition
import com.gh0u1l5.wechatmagician.util.ViewUtil.getListViewFromSnsActivity
import com.gh0u1l5.wechatmagician.util.ViewUtil.getViewAtPosition
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.*

object SnsForward {

    private val pkg = WechatPackage
    private val events = WechatEvents

    // Hook SnsUserUI.onCreate to popup a menu during long click.
    @JvmStatic fun setLongClickListenerForSnsUserUI() {
        if (pkg.SnsUserUI == null) {
            return
        }

        findAndHookMethod(pkg.SnsUserUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val listView = getListViewFromSnsActivity(param.thisObject) ?: return

                // Set onLongClickListener for items
                listView.setOnItemLongClickListener { parent, view, position, _ ->
                    val item = parent.getItemAtPosition(position)
                    val snsId = getLongField(item, "field_snsId")
                    events.onTimelineItemLongClick(parent, view, snsId, null)
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
        })
    }

    // Hook SnsTimeLineUI.onCreate to popup a menu during long click.
    @JvmStatic fun setLongClickListenerForSnsTimeLineUI() {
        if (pkg.SnsTimeLineUI == null) {
            return
        }

        findAndHookMethod(pkg.SnsTimeLineUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val listView = getListViewFromSnsActivity(param.thisObject) ?: return

                // Set onTouchListener for the list view.
                var lastKnownX = 0
                var lastKnownY = 0
                val detector = GestureDetector(listView.context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent?) {
                        val position = listView.pointToPosition(lastKnownX, lastKnownY)
                        val view = listView.getViewAtPosition(position)
                        val item = listView.getItemAtPosition(position)
                        val snsId = getLongField(item, "field_snsId")
                        val popup = ListPopupPosition(listView, lastKnownX, lastKnownY)
                        events.onTimelineItemLongClick(listView, view, snsId, popup)
                    }
                })
                (listView as View).setOnTouchListener { _, event ->
                    lastKnownX = event.x.toInt()
                    lastKnownY = event.y.toInt()
                    return@setOnTouchListener detector.onTouchEvent(event)
                }
            }
        })
    }

    // Hook SnsUploadUI.onCreate to clean EditText properly before forwarding.
    @JvmStatic fun cleanTextViewBeforeForwarding() {
        when (null) {
            pkg.SnsUploadUI,
            pkg.SnsUploadUIEditTextField -> return
        }

        findAndHookMethod(pkg.SnsUploadUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent ?: return
                if (intent.getBooleanExtra("Ksnsforward", false)) {
                    val content = intent.getStringExtra("Kdescription")
                    val editTextField = pkg.SnsUploadUIEditTextField
                    val editText = getObjectField(param.thisObject, editTextField)
                    XposedHelpers.callMethod(editText, "setText", content)
                }
            }
        })
    }
}