package com.gh0u1l5.wechatmagician.xposed

import android.content.ContentValues
import com.gh0u1l5.wechatmagician.util.MessageUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.System.currentTimeMillis

class WechatMessage(val msgId: Int, val type: Int, val talker: String, var content: String) {
    val time: Long = currentTimeMillis()
    init { if (type != 1) content = "" }
}

class WechatRevokeHook(var ver: WechatVersion) {

    var msgTable: List<WechatMessage> = listOf()

    fun hook(loader: ClassLoader?) {
        hookRevoke(loader)
        hookDatabase(loader)
    }

    @Synchronized
    private fun addMessage(msgId: Int, type: Int, talker: String, content: String) {
        msgTable += WechatMessage(msgId, type, talker,content)
    }

    @Synchronized
    private fun getMessage(msgId: Int): WechatMessage? {
        return msgTable.find { it.msgId == msgId }
    }

    @Synchronized
    private fun cleanMessage() {
        msgTable = msgTable.filter { currentTimeMillis() - it.time < 120000 }
    }

    @Suppress("UNCHECKED_CAST")
    private fun hookRevoke(loader: ClassLoader?) {
        if (ver.recallClass == "" || ver.recallMethod == "") return

        XposedHelpers.findAndHookMethod(ver.recallClass, loader, ver.recallMethod, String::class.java, String::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                param.result = (param.result as? MutableMap<String, String?>)?.apply {
                    if (this[".sysmsg.\$type"] != "revokemsg") return
                    this[".sysmsg.revokemsg.replacemsg"] = this[".sysmsg.revokemsg.replacemsg"]?.let {
                        if (it.startsWith("你") || it.toLowerCase().startsWith("you")) it
                        else MessageUtil.customize(it)
                    }
                }
            }
        })
    }

    private fun hookDatabase(loader: ClassLoader?) {

        XposedHelpers.findAndHookMethod(ver.SQLiteDatabaseClass, loader, "insertWithOnConflict", String::class.java, String::class.java, ContentValues::class.java, Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val p1 = param.args[0] as String?
                val p2 = param.args[1] as String?
                val p3 = param.args[2] as ContentValues?
                val p4 = param.args[3] as Int

                if (p1 == "message") {
                    p3?.apply {
                        if (!containsKey("type") || !containsKey("talker")) {
                            XposedBridge.log("DB => insert p1 = $p1, p2 = $p2, p3 = $p3, p4 = $p4")
                            return
                        }
                        addMessage(getAsInteger("msgId"), getAsInteger("type"), getAsString("talker"), getAsString("content"))
                    }
                    cleanMessage()
                }
            }
        })

        XposedHelpers.findAndHookMethod(ver.SQLiteDatabaseClass, loader, "updateWithOnConflict", String::class.java, ContentValues::class.java, String::class.java, Array<String?>::class.java, Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val p1 = param.args[0] as String?
                val p2 = param.args[1] as ContentValues?
//                val p3 = param.args[2] as String?
//                val p4 = param.args[3] as Array<*>?
//                val p5 = param.args[4] as Int
//                XposedBridge.log("DB => update p1 = $p1, p2 = $p2, p3 = $p3, p4 = ${MessageUtil.argsToString(p4)}, p5 = $p5")

                if (p1 == "message") {
                    p2?.apply {
                        if (getAsInteger("type") != 10000){
                            return
                        }

                        val sysMsg = getAsString("content")
                        if (sysMsg.startsWith("你") || sysMsg.toLowerCase().startsWith("you")) {
                            return
                        }

                        remove("content"); remove("type")
                        getMessage(getAsInteger("msgId"))?.let {
                            if (it.type != 1) return
                            if (it.talker.contains("chatroom"))
                                put("content", MessageUtil.notifyChatroomRecall("[已撤回]", it.content))
                            else
                                put("content", MessageUtil.notifyPrivateRecall("[已撤回]", it.content))
                        }
                    }
                }
                if (p1 == "SnsInfo") {
                    p2?.apply {
                        if (!containsKey("sourceType") || this["sourceType"] != 0){
                            return
                        }
                        put("content", MessageUtil.notifyInfoDelete("[已删除]", getAsByteArray("content")))
                        remove("sourceType")
                    }
                }
                if (p1 == "SnsComment") {
                    p2?.apply {
                        if (!containsKey("type") || !containsKey("commentflag")){
                            return
                        }
                        if (this["type"] == 1 || this["commentflag"] != 1){
                            return
                        }
                        put("curActionBuf", MessageUtil.notifyCommentDelete("[已删除]", getAsByteArray("curActionBuf")))
                        remove("commentflag")
                    }
                }
            }
        })

//        XposedHelpers.findAndHookMethod(ver.SQLiteDatabaseClass, loader, "delete", String::class.java, String::class.java, Array<String?>::class.java, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val p1 = param.args[0] as String?
//                val p2 = param.args[1] as String?
//                val p3 = param.args[2] as Array<*?>?
//                XposedBridge.log("DB => delete p1 = $p1, p2 = $p2, p3 = ${MessageUtil.argsToString(p3)}")
//            }
//        })

//        XposedHelpers.findAndHookMethod(ver.SQLiteDatabaseClass, loader, "executeSql", String::class.java, Array<Any?>::class.java, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val p1 = param.args[0] as String?
//                val p2 = param.args[1] as Array<*?>?
//                XposedBridge.log("DB => executeSqlxecSQL p1 = $p1, p2 = ${MessageUtil.argsToString(p2)}")
//            }
//        })
    }
}
