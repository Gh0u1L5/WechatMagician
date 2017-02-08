package com.rarnu.norevoke.util

import de.robv.android.xposed.XposedBridge

/**
 * Created by rarnu on 1/13/17.
 */
object MessageUtil {

    fun customize(str: String?): String? {
        // XposedBridge.log("extractContent => str: $str")
        var _replace = str!!
        _replace = _replace.substring(1)
        _replace = _replace.substring(0, _replace.indexOf("\""))
        _replace = "  $_replace 妄图撤回一条消息，啧啧  "
        return _replace
    }

    fun argsToString(arg: Array<String?>?): String? {
        var ret = ""
        arg?.forEach { ret += "$it," }
        return ret
    }

    fun argsToString(args: Array<Any?>?): String? {
        var ret = ""
        args?.forEach { ret += "${it.toString()}," }
        return ret
    }
}