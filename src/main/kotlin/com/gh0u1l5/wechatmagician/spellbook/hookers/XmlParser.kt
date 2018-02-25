package com.gh0u1l5.wechatmagician.spellbook.hookers

import com.gh0u1l5.wechatmagician.spellbook.Global.STATUS_FLAG_XML_PARSER
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.XMLParseMethod
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.XMLParserClass
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.hookers.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IXmlParserHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IXmlParserHookRaw
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook

object XmlParser : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IXmlParserHook::class.java, IXmlParserHookRaw::class.java)

    @Suppress("UNCHECKED_CAST")
    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(XMLParserClass, XMLParseMethod, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeXmlParse", { plugin ->
                    if (plugin is IXmlParserHookRaw) {
                        plugin.beforeXmlParse(param)
                    }
                })
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterXmlParse", { plugin ->
                    if (plugin is IXmlParserHookRaw) {
                        plugin.afterXmlParse(param)
                    }
                })
                notify("onXmlParse", { plugin ->
                    if (plugin is IXmlParserHook) {
                        val root = param.args[1] as String
                        val xml = param.result as MutableMap<String, String>?
                        if (xml != null) {
                            plugin.onXmlParse(root, xml)
                        }
                    }
                })
            }
        })

        WechatStatus.toggle(STATUS_FLAG_XML_PARSER, true)
    }
}