package com.gh0u1l5.wechatmagician.backend.plugins

import com.gh0u1l5.wechatmagician.Global.SETTINGS_SNS_KEYWORD_BLACKLIST
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SNS_KEYWORD_BLACKLIST_CONTENT
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.storage.list.SnsBlacklist
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IXmlParserHook

object SnsBlock : IXmlParserHook {

    private const val ROOT_TAG    = "TimelineObject"
    private const val CONTENT_TAG = ".TimelineObject.contentDesc"
    private const val ID_TAG      = ".TimelineObject.id"
    private const val PRIVATE_TAG = ".TimelineObject.private"

    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_SNS_KEYWORD_BLACKLIST, false)

    override fun onXmlParsed(root: String, xml: MutableMap<String, String>) {
        if (!isPluginEnabled()) {
            return
        }
        if (root == ROOT_TAG && xml[PRIVATE_TAG] != "1") {
            val content = xml[CONTENT_TAG] ?: return
            val keywords = pref.getStringList(SETTINGS_SNS_KEYWORD_BLACKLIST_CONTENT, emptyList())
            keywords.forEach { keyword ->
                if (content.contains(keyword)) {
                    SnsBlacklist += xml[ID_TAG]
                    xml[PRIVATE_TAG] = "1"
                    return
                }
            }
        }
    }
}
