package com.gh0u1l5.wechatmagician.backend.plugins

import android.content.ContentValues
import com.gh0u1l5.wechatmagician.Global.SETTINGS_CHATTING_RECALL
import com.gh0u1l5.wechatmagician.Global.SETTINGS_CHATTING_RECALL_PROMPT
import com.gh0u1l5.wechatmagician.Global.tryWithLog
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.WechatPackage.MsgStorageInsertMethod
import com.gh0u1l5.wechatmagician.backend.WechatPackage.MsgStorageObject
import com.gh0u1l5.wechatmagician.backend.interfaces.IDatabaseHookRaw
import com.gh0u1l5.wechatmagician.backend.interfaces.IXmlParserHook
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_RECALL
import com.gh0u1l5.wechatmagician.storage.cache.MessageCache
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object AntiRevoke : IDatabaseHookRaw, IXmlParserHook {

    private const val ROOT_TAG        = "sysmsg"
    private const val TYPE_TAG        = ".sysmsg.\$type"
    private const val REPLACE_MSG_TAG = ".sysmsg.revokemsg.replacemsg"

    private val str = LocalizedStrings
    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_CHATTING_RECALL, true)

    override fun beforeDatabaseUpdate(param: XC_MethodHook.MethodHookParam) {
        if (!isPluginEnabled()) {
            return
        }
        val table = param.args[0] as String? ?: return
        val values = param.args[1] as ContentValues? ?: return
        if (table == "message" && values["type"] == 10000) {
            if (values.getAsString("content").startsWith("\"")) {
                handleMessageRecall(values)
                param.result = 1
            }
        }
    }

    override fun beforeDatabaseDelete(param: XC_MethodHook.MethodHookParam) {
        if (!isPluginEnabled()) {
            return
        }
        val table = param.args[0] as String?
        when (table) {
            "ImgInfo2", "videoinfo2", "WxFileIndex2" -> param.result = 1
        }
    }

    override fun onXmlParse(root: String, xml: MutableMap<String, String>) {
        if (!isPluginEnabled()) {
            return
        }
        if (root == ROOT_TAG && xml[TYPE_TAG] == "revokemsg") {
            val msg = xml[REPLACE_MSG_TAG] ?: return
            if (msg.startsWith("\"")) {
                val prompt = pref.getString(SETTINGS_CHATTING_RECALL_PROMPT, str[PROMPT_RECALL])
                xml[REPLACE_MSG_TAG] = MessageUtil.applyEasterEgg(msg, prompt)
            }
        }
    }

    // handleMessageRecall notifies user that someone has recalled the given message.
    private fun handleMessageRecall(values: ContentValues) {
        if (MsgStorageObject == null) {
            return
        }

        tryWithLog {
            val msgId = values["msgId"] as Long
            val msg = MessageCache[msgId] ?: return@tryWithLog

            val copy = msg.javaClass.newInstance()
            PackageUtil.shadowCopy(msg, copy)

            val createTime = XposedHelpers.getLongField(msg, "field_createTime")
            XposedHelpers.setIntField(copy, "field_type", values["type"] as Int)
            XposedHelpers.setObjectField(copy, "field_content", values["content"])
            XposedHelpers.setLongField(copy, "field_createTime", createTime + 1L)

            when (MsgStorageInsertMethod.parameterTypes.size) {
                1 -> MsgStorageInsertMethod.invoke(MsgStorageObject, copy)
                2 -> MsgStorageInsertMethod.invoke(MsgStorageObject, copy, false)
            }
        }
    }
}