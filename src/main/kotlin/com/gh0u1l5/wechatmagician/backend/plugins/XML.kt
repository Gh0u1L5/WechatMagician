package com.gh0u1l5.wechatmagician.backend.plugins

import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.HookStatus
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.SnsCache
import com.gh0u1l5.wechatmagician.storage.Strings
import com.gh0u1l5.wechatmagician.util.MessageUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import kotlin.concurrent.thread

class XML(private val preferences: Preferences) {

    private val str = Strings
    private val pkg = WechatPackage

    fun hookXMLParse() {
        if (pkg.XMLParserClass == null || pkg.XMLParseMethod == "") {
            return
        }

        // Hook XML Parser for the status bar easter egg.
        XposedHelpers.findAndHookMethod(pkg.XMLParserClass, pkg.XMLParseMethod, C.String, C.String, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                @Suppress("UNCHECKED_CAST")
                val result = param.result as MutableMap<String, String?>? ?: return
                if (result[".sysmsg.\$type"] == "revokemsg") {
                    val msgTag = ".sysmsg.revokemsg.replacemsg"
                    val msg = result[msgTag] ?: return
                    if (!msg.startsWith("\"")) {
                        return
                    }
                    if (!preferences.getBoolean("settings_chatting_recall", true)) {
                        return
                    }
                    val prompt = preferences.getString(
                            "settings_chatting_recall_prompt", str["prompt_recall"])
                    result[msgTag] = MessageUtil.applyEasterEgg(msg, prompt)
                }
                if (result[".TimelineObject"] != null) {
                    thread(start = true) {
                        val id = result[".TimelineObject.id"]
                        if (id != null) {
                            SnsCache[id] = SnsCache.SnsInfo(result)
                        }
                    }
                }
            }
        })

        HookStatus.XMLParser = true
    }
}