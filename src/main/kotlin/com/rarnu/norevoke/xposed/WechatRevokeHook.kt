package com.rarnu.norevoke.xposed

import android.content.ContentValues
import com.rarnu.norevoke.util.MessageUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers


/**
 * Created by rarnu on 1/11/17.
 */
class WechatRevokeHook(ver: WechatVersion) {

    var _v: WechatVersion? = null
    var _db: WechatDatabase? = null

    init { _v = ver }

    fun hook(loader: ClassLoader?) {
        try { hookRevoke(loader) } catch (t: Throwable) { }
        try { hookDatabase(loader) } catch (t: Throwable) { }
    }

    private fun hookRevoke(loader: ClassLoader?) {

        XposedHelpers.findAndHookMethod(_v?.recallClass, loader, _v?.recallMethod, String::class.java, String::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val m = param.result as MutableMap<String, String?>?
                if (m != null) {
                    val type = m[".sysmsg.\$type"]
                    if (type == null || type != "revokemsg") return

                    val replaceMsg = m[".sysmsg.revokemsg.replacemsg"]!!
                    if (replaceMsg.startsWith("你") || replaceMsg.toLowerCase().startsWith("you")) return

                    try {
                        m[".sysmsg.revokemsg.replacemsg"] = MessageUtil.customize(replaceMsg)
                        param.result = m
                    } catch (t: Throwable) { }
                }
            }
        })
    }

    private fun hookDatabase(loader: ClassLoader?) {
        XposedHelpers.findAndHookConstructor(_v?.storageClass, loader, _v?.storageMethod, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (_db == null)
                    try { _db = WechatDatabase(param.args[0]) } catch (t: Throwable) { }
            }
        })

        /*
        public final long insertWithOnConflict(String str, String str2, ContentValues contentValues, int i)
        public final int updateWithOnConflict(String str, ContentValues contentValues, String str2, String[] strArr, int i)
        public final int delete(String str, String str2, String[] strArr)
        private int executeSql(String str, Object[] objArr)
         */
        XposedHelpers.findAndHookMethod(_v?.SQLiteDatabaseClass, loader, "insertWithOnConflict", String::class.java, String::class.java, ContentValues::class.java, Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val p1 = param.args[0] as String?
                val p2 = param.args[1] as String?
                val p3 = param.args[2] as ContentValues?
                val p4 = param.args[3] as Int
                if (p1 == "message")
                    XposedBridge.log("DB => insert p1 = $p1, p2 = $p2, p3 = ${p3.toString()}, p4 = $p4")
            }
        })

        XposedHelpers.findAndHookMethod(_v?.SQLiteDatabaseClass, loader, "updateWithOnConflict", String::class.java, ContentValues::class.java, String::class.java, Array<String?>::class.java, Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val p1 = param.args[0] as String?
                val p2 = param.args[1] as ContentValues?
                val p3 = param.args[2] as String?
                val p4 = param.args[3] as Array<String?>?
                val p5 = param.args[4] as Int
                if (p1 == "message") {
                    XposedBridge.log("DB => update p1 = $p1, p2 = ${p2.toString()}, p3 = $p3, p4 = ${MessageUtil.argsToString(p4)}, p5 = $p5")
                    if (p2 == null || p2["type"] != 10000 || p2.containsKey("isSend")) return

                    p2.remove("type")

                    val cur = _db?.getMessageViaId(p2["msgId"].toString());
                    if (cur == null || !cur.moveToFirst()) return
                    p2.put("content", "[已撤回] ${cur.getString(cur.getColumnIndex("content"))}")
                    cur.close()
                }
            }
        })

        XposedHelpers.findAndHookMethod(_v?.SQLiteDatabaseClass, loader, "delete", String::class.java, String::class.java, Array<String?>::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val p1 = param.args[0] as String?
                val p2 = param.args[1] as String?
                val p3 = param.args[2] as Array<String?>?
                XposedBridge.log("DB => delete p1 = $p1, p2 = $p2, p3 = ${p3.toString()}")
            }
        })
        XposedHelpers.findAndHookMethod(_v?.SQLiteDatabaseClass, loader, "executeSql", String::class.java, Array<Any?>::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val p1 = param.args[0] as String?
                val p2 = param.args[1] as Array<Any?>?
                XposedBridge.log("DB => execSQL p1 = $p1, p2 = ${MessageUtil.argsToString(p2)}")
            }
        })

    }

}