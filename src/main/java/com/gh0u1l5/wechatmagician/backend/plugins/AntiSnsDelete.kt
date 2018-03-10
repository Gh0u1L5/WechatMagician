package com.gh0u1l5.wechatmagician.backend.plugins

import android.content.ContentValues
import com.gh0u1l5.wechatmagician.Global
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.LABEL_DELETED
import com.gh0u1l5.wechatmagician.backend.storage.list.SnsBlacklist
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import com.gh0u1l5.wechatmagician.util.MessageUtil
import de.robv.android.xposed.XC_MethodHook

object AntiSnsDelete : IDatabaseHook {

    private val str = LocalizedStrings
    private val pref = WechatHook.settings

    override fun onDatabaseUpdating(param: XC_MethodHook.MethodHookParam) {
        val table = param.args[0] as String? ?: return
        val values = param.args[1] as ContentValues? ?: return

        when (table) {
            "SnsInfo" -> { // delete moment
                if (values["type"] !in listOf(1, 2, 3, 15)) {
                    return
                }
                if (values["sourceType"] != 0) {
                    return
                }
                if (values["stringSeq"] in SnsBlacklist) {
                    return
                }
                if (!pref.getBoolean(Global.SETTINGS_SNS_DELETE_MOMENT, true)) {
                    return
                }
                val content = values["content"] as ByteArray?
                handleMomentDelete(content, values)
            }
            "SnsComment" -> { // delete moment comment
                if (values["type"] == 1) {
                    return
                }
                if (values["commentflag"] != 1) {
                    return
                }
                if (!pref.getBoolean(Global.SETTINGS_SNS_DELETE_COMMENT, true)) {
                    return
                }
                val curActionBuf = values["curActionBuf"] as ByteArray?
                handleCommentDelete(curActionBuf, values)
            }
        }
    }

    // handleMomentDelete notifies user that someone has deleted the given moment.
    private fun handleMomentDelete(content: ByteArray?, values: ContentValues) {
        MessageUtil.notifyInfoDelete(str[LABEL_DELETED], content)?.let { msg ->
            values.remove("sourceType")
            values.put("content", msg)
        }
    }

    // handleCommentDelete notifies user that someone has deleted the given comment in moments.
    private fun handleCommentDelete(curActionBuf: ByteArray?, values: ContentValues) {
        MessageUtil.notifyCommentDelete(str[LABEL_DELETED], curActionBuf)?.let { msg ->
            values.remove("commentflag")
            values.put("curActionBuf", msg)
        }
    }
}