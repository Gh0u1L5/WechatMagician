package com.gh0u1l5.wechatmagician.backend.interfaces

import de.robv.android.xposed.XC_MethodHook

interface IXmlParserHookRaw {

    // Wechat.SDK.XMLParser.parse(xml: String, tag: String): Map<String, String>
    fun beforeXMLParse(param: XC_MethodHook.MethodHookParam) { }
    fun afterXMLParse(param: XC_MethodHook.MethodHookParam) { }
}