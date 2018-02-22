package com.gh0u1l5.wechatmagician.backend.plugins

import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.interfaces.IXmlParserHookRaw
import de.robv.android.xposed.XC_MethodHook

object AdBlock : IXmlParserHookRaw {

    // TODO: Add configuration in frontend

    private val pref = WechatHook.settings

    private fun isPluginEnabled() = true

    override fun beforeXMLParse(param: XC_MethodHook.MethodHookParam) {
        if (!isPluginEnabled()) {
            return
        }
        val root = param.args[1] as String
        if (root == "ADInfo") {
            param.result = null
        }
    }
}