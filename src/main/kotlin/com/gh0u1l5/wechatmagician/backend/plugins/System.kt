package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.util.MessageUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

class System(private val loader: ClassLoader, private val preferences: Preferences) {

    private val pkg = WechatPackage

    // Hook View.onTouchEvent to trace touch events.
    fun traceTouchEvents() {
        XposedHelpers.findAndHookMethod(
                "android.view.View", loader,
                "onTouchEvent", C.MotionEvent, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (preferences.getBoolean("developer_ui_touch_event", false)) {
                    XposedBridge.log("View.onTouchEvent => obj.class = ${param.thisObject.javaClass}")
                }
            }
        })
    }

    // Hook Activity.startActivity and Activity.onCreate to trace activities.
    fun traceActivities() {
        XposedHelpers.findAndHookMethod(
                "android.app.Activity", loader,
                "startActivity", C.Intent, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (preferences.getBoolean("developer_ui_trace_activities", false)) {
                    val intent = param.args[0] as Intent?
                    XposedBridge.log("Activity.startActivity => ${param.thisObject.javaClass}, intent => ${MessageUtil.bundleToString(intent?.extras)}")
                }
            }
        })

        XposedHelpers.findAndHookMethod(
                "android.app.Activity", loader,
                "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (preferences.getBoolean("developer_ui_trace_activities", false)) {
                    val bundle = param.args[0] as Bundle?
                    val intent = (param.thisObject as Activity).intent
                    XposedBridge.log("Activity.onCreate => ${param.thisObject.javaClass}, intent => ${MessageUtil.bundleToString(intent?.extras)}, bundle => ${MessageUtil.bundleToString(bundle)}")
                }
            }
        })
    }

    // Hook XLog to print internal errors into logcat.
    fun enableXLog() {
        if (pkg.XLogSetup == null) {
            return
        }

        XposedBridge.hookAllMethods(pkg.XLogSetup, "keep_setupXLog", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (preferences.getBoolean("developer_ui_xlog", false)) {
                    param.args[5] = true // enable logcat output
                }
            }
        })
    }
}