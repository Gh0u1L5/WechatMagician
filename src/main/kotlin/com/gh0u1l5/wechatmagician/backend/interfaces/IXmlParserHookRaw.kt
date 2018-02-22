package com.gh0u1l5.wechatmagician.backend.interfaces

import de.robv.android.xposed.XC_MethodHook

interface IXmlParserHookRaw {
    // Wechat.SDK.XMLParser.parse(xml: String, tag: String): Map<String, String>
    fun beforeXmlParse(param: XC_MethodHook.MethodHookParam) { }
    fun afterXmlParse(param: XC_MethodHook.MethodHookParam) { }
}