package com.gh0u1l5.wechatmagician.spellbook.interfaces

interface IXmlParserHook {
    // IXmlParserHookRaw.afterXmlParse(param: MethodHookParam)
    //     root => param.args[1] as String
    //     xml  => param.result as MutableMap<String, String>
    fun onXmlParse(root: String, xml: MutableMap<String, String>) { }
}