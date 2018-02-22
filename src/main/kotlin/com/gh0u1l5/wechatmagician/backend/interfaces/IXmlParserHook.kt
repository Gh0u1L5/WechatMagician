package com.gh0u1l5.wechatmagician.backend.interfaces

interface IXmlParserHook {
    // IXmlParserHookRaw.afterXMLParse(param: MethodHookParam)
    //     root => param.args[1] as String
    //     xml  => param.result as MutableMap<String, String>
    fun onXMLParse(root: String, xml: MutableMap<String, String>) { }
}