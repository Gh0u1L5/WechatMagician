package com.rarnu.norevoke.xposed

import android.content.ContentValues
import com.rarnu.norevoke.util.MessageUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.lang.System.currentTimeMillis
import java.util.*
import kotlin.concurrent.timer

class WechatMessage(type: Int, talker: String, content: String) {
    var _time: Long = currentTimeMillis()
    var _type: Int = -1
    var _talker: String = ""
    var _content: String = ""
    init { _type = type; _talker = talker; _content = content }
}

class WechatRevokeHook(ver: WechatVersion) {

    var _v: WechatVersion? = null
    var _msgTable: HashMap<Int, WechatMessage> = hashMapOf()

    init {
        _v = ver
        timer("msgTableCleaner", true, 0, 180000, {
            for ((key, value) in _msgTable) {
                if (currentTimeMillis() - value._time > 120000) {
                    _msgTable.remove(key)
                }
            }
        })
    }

    fun hook(loader: ClassLoader?) {
        try { hookRevoke(loader) } catch (t: Throwable) { }
        try { hookDatabase(loader) } catch (t: Throwable) { }
    }

    private fun hookRevoke(loader: ClassLoader?) {
        if (_v == null || _v?.recallClass == "" || _v?.recallMethod == "") return

        XposedHelpers.findAndHookMethod(_v?.recallClass, loader, _v?.recallMethod, String::class.java, String::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                param.result = (param.result as MutableMap<String, String?>).apply {
                    if (this[".sysmsg.\$type"] != "revokemsg") return
                    this[".sysmsg.revokemsg.replacemsg"] = this[".sysmsg.revokemsg.replacemsg"]?.let {
                        if (it.startsWith("你") || it.toLowerCase().startsWith("you"))
                            "_ignore_" + it
                        else
                            MessageUtil.customize(it)
                    }
                }
            }
        })
    }

    private fun hookDatabase(loader: ClassLoader?) {

        XposedHelpers.findAndHookMethod(_v?.SQLiteDatabaseClass, loader, "insertWithOnConflict", String::class.java, String::class.java, ContentValues::class.java, Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val p1 = param.args[0] as String?
                val p2 = param.args[1] as String?
                val p3 = param.args[2] as ContentValues?
                val p4 = param.args[3] as Int
//                XposedBridge.log("DB => insert p1 = $p1, p2 = $p2, p3 = ${p3?.toString()}, p4 = $p4")
                if (p1 == "message") {
                    p3?.apply {
                        val msg = WechatMessage(getAsInteger("type"), getAsString("talker"), getAsString("content"))
                        _msgTable.put(getAsInteger("msgId"), msg)
                    }
                }
            }
        })

        XposedHelpers.findAndHookMethod(_v?.SQLiteDatabaseClass, loader, "updateWithOnConflict", String::class.java, ContentValues::class.java, String::class.java, Array<String?>::class.java, Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val p1 = param.args[0] as String?
                val p2 = param.args[1] as ContentValues?
                val p3 = param.args[2] as String?
                val p4 = param.args[3] as Array<*>?
                val p5 = param.args[4] as Int
//                XposedBridge.log("DB => update p1 = $p1, p2 = ${p2?.toString()}, p3 = $p3, p4 = ${MessageUtil.argsToString(p4)}, p5 = $p5")
                if (p1 == "message") {
                    p2?.apply {
                        if (getAsInteger("type") != 10000) return
                        if (getAsString("content").startsWith("_ignore_")) return

                        remove("content"); remove("type")
                        _msgTable[getAsInteger("msgId")]?.apply {
                            if (_type == 1) {
                                if (_talker.contains("chatroom"))
                                    put("content", MessageUtil.notifyChatroomRecall("[已撤回]", _content))
                                else
                                    put("content", MessageUtil.notifyPrivateRecall("[已撤回]", _content))
                            }
                        }
                    }
                }
//                if (p1 == "SnsInfo") {
//                    p2?.apply {
//                        XposedBridge.log("DB => update p1 = $p1, p2 = ${p2.toString()}, p3 = $p3, p4 = ${MessageUtil.argsToString(p4)}, p5 = $p5")
//                        XposedBridge.log("DB => content = ${MessageUtil.bytesToHexString(getAsByteArray("content"))}")
//                        if (containsKey("sourceType") && this["sourceType"] == 0) {
//                            put("content", MessageUtil.notifyInfoDelete("[已删除]", getAsByteArray("content")))
//                            remove("sourceType")
//                            XposedBridge.log("DB => modifyd = ${MessageUtil.bytesToHexString(getAsByteArray("content"))}")
//                        }
//                    }
//                }
//                if (p1 == "SnsComment") {
//                    p2?.apply {
//                        if (containsKey("type") && this["type"] == 1) return
//                        if (containsKey("commentflag") && this["commentflag"] == 1) {
//                            put("curActionBuf", MessageUtil.notifyCommentDelete("[已删除]", getAsByteArray("curActionBuf")))
//                            remove("commentflag")
//                        }
//                    }
//                }
            }
        })

//        XposedHelpers.findAndHookMethod(_v?.SQLiteDatabaseClass, loader, "delete", String::class.java, String::class.java, Array<String?>::class.java, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val p1 = param.args[0] as String?
//                val p2 = param.args[1] as String?
//                val p3 = param.args[2] as Array<*?>?
//                XposedBridge.log("DB => delete p1 = $p1, p2 = $p2, p3 = ${MessageUtil.argsToString(p3)}")
//            }
//        })

//        XposedHelpers.findAndHookMethod(_v?.SQLiteDatabaseClass, loader, "executeSql", String::class.java, Array<Any?>::class.java, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val p1 = param.args[0] as String?
//                val p2 = param.args[1] as Array<*?>?
//                XposedBridge.log("DB => executeSqlxecSQL p1 = $p1, p2 = ${MessageUtil.argsToString(p2)}")
//            }
//        })

    }
}