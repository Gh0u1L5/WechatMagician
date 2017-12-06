package com.gh0u1l5.wechatmagician.backend.plugins

import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_XML_PARSER
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.Strings
import com.gh0u1l5.wechatmagician.storage.Strings.PROMPT_RECALL
import com.gh0u1l5.wechatmagician.storage.cache.SnsCache
import com.gh0u1l5.wechatmagician.storage.list.SnsBlacklist
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook
import kotlin.concurrent.thread

object XML {

    private var preferences: Preferences? = null

    @JvmStatic fun init(_preferences: Preferences) {
        preferences = _preferences
    }

    private val str = Strings
    private val pkg = WechatPackage

    @JvmStatic fun hookXMLParse() {
        if (pkg.XMLParserClass == null || pkg.XMLParseMethod == null) {
            return
        }

        // Hook XML Parser for the status bar easter egg.
        findAndHookMethod(pkg.XMLParserClass, pkg.XMLParseMethod, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                @Suppress("UNCHECKED_CAST")
                val result = param.result as MutableMap<String, String?>? ?: return
                if (result[".sysmsg.\$type"] == "revokemsg") {
                    handleRevokeCommand(result)
                }
                if (result[".TimelineObject"] != null) {
                    matchKeywordBlackList(result)
                    recordTimelineObject(result)
                }
            }
        })

        pkg.setStatus(STATUS_FLAG_XML_PARSER, true)
    }

    private fun handleRevokeCommand(result: MutableMap<String, String?>) {
        val msgTag = ".sysmsg.revokemsg.replacemsg"
        val msg = result[msgTag] ?: return
        if (!msg.startsWith("\"")) {
            return
        }
        if (!preferences!!.getBoolean("settings_chatting_recall", true)) {
            return
        }
        val prompt = preferences!!.getString(
                "settings_chatting_recall_prompt", str[PROMPT_RECALL])
        result[msgTag] = MessageUtil.applyEasterEgg(msg, prompt)
    }

    private fun matchKeywordBlackList(result: MutableMap<String, String?>) {
        if (!preferences!!.getBoolean("settings_sns_keyword_blacklist", false)) {
            return
        }
        val content = result[".TimelineObject.contentDesc"] ?: return
        val list = preferences!!.getStringList("settings_sns_keyword_blacklist_content", listOf())
        list.filter { content.contains(it) }.forEach {
            result[".TimelineObject.private"] = "1"
            SnsBlacklist += result[".TimelineObject.id"]
            return
        }
    }

    private fun recordTimelineObject(result: MutableMap<String, String?>) {
        thread(start = true) {
            val id = result[".TimelineObject.id"]
            if (id != null) {
                SnsCache[id] = SnsCache.SnsInfo(result)
            }
        }
    }
}