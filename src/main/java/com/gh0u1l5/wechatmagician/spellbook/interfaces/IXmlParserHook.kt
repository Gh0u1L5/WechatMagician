package com.gh0u1l5.wechatmagician.spellbook.interfaces

import de.robv.android.xposed.XC_MethodHook

interface IXmlParserHook {

    // Wechat.SDK.XMLParser.parse(xml: String, tag: String): Map<String, String>
    fun onXmlParsing(param: XC_MethodHook.MethodHookParam) { }
    fun onXmlParsed(root: String, xml: MutableMap<String, String>) { }
}