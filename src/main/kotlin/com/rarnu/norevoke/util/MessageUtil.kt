package com.rarnu.norevoke.util

import de.robv.android.xposed.XposedBridge

/**
 * Created by rarnu on 1/13/17.
 */
object MessageUtil {

    fun extractContent(replace: String?, str: String?): String? {
        // XposedBridge.log("extractContent => replace: $replace, str: $str")
        var _replace = replace!!
        var _str = str!!
        _replace = _replace.substring(1)
        _replace = _replace.substring(0, _replace.indexOf("\""))

        if (_str.contains(":\n")) {
            if (_str.substringBefore(":\n").length <= 32) {
                _str = _str.substring(_str.indexOf(":\n") + 2)
            }
        }

        _str = _str.replace("\n", "\n  ")
        _replace = "  $_replace 试图撤回一条消息:  \n  $_str  "
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