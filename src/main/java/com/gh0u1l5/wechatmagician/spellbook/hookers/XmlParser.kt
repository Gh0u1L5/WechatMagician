package com.gh0u1l5.wechatmagician.spellbook.hookers

import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IXmlParserHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.sdk.platformtools.Classes.XmlParser
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.sdk.platformtools.Methods.XmlParser_parse
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook

object XmlParser : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IXmlParserHook::class.java)

    @Suppress("UNCHECKED_CAST")
    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(XmlParser, XmlParser_parse, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onXmlParsing") { plugin ->
                    (plugin as IXmlParserHook).onXmlParsing(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                val root = param.args[1] as String
                val xml  = param.result as MutableMap<String, String>? ?: return
                notify("onXmlParsed") { plugin ->
                    (plugin as IXmlParserHook).onXmlParsed(root, xml)
                }
            }
        })

        WechatStatus.toggle(WechatStatus.StatusFlag.STATUS_FLAG_XML_PARSER, true)
    }
}