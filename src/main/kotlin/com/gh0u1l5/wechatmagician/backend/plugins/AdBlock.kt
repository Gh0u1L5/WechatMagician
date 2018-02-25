package com.gh0u1l5.wechatmagician.backend.plugins

import com.gh0u1l5.wechatmagician.Global.SETTINGS_SNS_ADBLOCK
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IXmlParserHook
import de.robv.android.xposed.XC_MethodHook

object AdBlock : IXmlParserHook {

    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_SNS_ADBLOCK, true)

    // Interrupt the XML parsing for if the root tag is "ADInfo".
    override fun onXmlParsing(param: XC_MethodHook.MethodHookParam) {
        if (!isPluginEnabled()) {
            return
        }
        val root = param.args[1] as String
        if (root == "ADInfo") {
            param.result = null
        }
    }
}