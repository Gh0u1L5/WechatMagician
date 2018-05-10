package com.gh0u1l5.wechatmagician.backend.plugins

import com.gh0u1l5.wechatmagician.Global.SETTINGS_SNS_ADBLOCK
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.base.Operation.Companion.nop
import com.gh0u1l5.wechatmagician.spellbook.base.Operation.Companion.replacement
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IXmlParserHook

object AdBlock : IXmlParserHook {

    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_SNS_ADBLOCK, true)

    override fun onXmlParsing(xml: String, root: String): Operation<MutableMap<String, String>?> {
        if (!isPluginEnabled()) {
            return nop()
        }
        if (root != "ADInfo") {
            return nop()
        }
        return replacement(null)
    }
}