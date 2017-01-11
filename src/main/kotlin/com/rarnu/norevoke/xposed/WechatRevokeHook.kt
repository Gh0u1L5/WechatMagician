package com.rarnu.norevoke.xposed

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * Created by rarnu on 1/11/17.
 */
class WechatRevokeHook {

    var v: WechatVersion? = null
    var db: WechatDatabase? = null

    constructor(ver: WechatVersion) {
        v = ver
    }

    fun hook(loader: ClassLoader?) {
        try { hookRevoke(loader) } catch (t: Throwable) { }
        try { hookDatabase(loader) } catch (t: Throwable) { }
        try { hookApplicationPackageManager(loader) } catch (t: Throwable) { }
    }

    private fun hookRevoke(loader: ClassLoader?) {
        XposedHelpers.findAndHookMethod(v?.recallClass, loader, v?.recallMethod, String::class.java, String::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val m = param.result as MutableMap<String, String?>
                val type = m[".sysmsg.\$type"]
                if (type == "revokemsg") {
                    val talker = m[".sysmsg.revokemsg.session"]
                    var replaceMsg = m[".sysmsg.revokemsg.replacemsg"]!!
                    val msgSvrId = m[".sysmsg.revokemsg.newmsgid"]
                    if (replaceMsg.startsWith("你") || replaceMsg.toLowerCase().startsWith("you")) {
                        return
                    }
                    val strings = replaceMsg.split("\"")
                    replaceMsg = "\"" + strings[1] + "\" " + "尝试撤回上一条消息 （已阻止)"
                    m.put(".sysmsg.\$type", null)
                    param.result = m

                    try {
                        val cur = db?.getMessageViaId(msgSvrId)
                        if (cur == null || !cur.moveToFirst()) {
                            return
                        }
                        val createTime = cur.getLong(cur.getColumnIndex("createTime"))
                        val idx = cur.getColumnIndex("talkerId")
                        var talkerId = -1
                        if (idx != -1) {
                            talkerId = cur.getInt(cur.getColumnIndex("talkerId"))
                        }
                        cur.close()
                        db?.insertSystemMessage(talker, talkerId, replaceMsg, createTime + 1)
                    } catch (t: Throwable) {

                    }
                }
            }
        })
    }

    private fun hookDatabase(loader: ClassLoader?) {
        XposedHelpers.findAndHookConstructor(v?.storageClass, loader, v?.storageMethod, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (db == null) {
                    try {
                        db = WechatDatabase(param.args[0])
                    } catch (t: Throwable) {

                    }
                }
            }
        })
    }

    private fun hookApplicationPackageManager(loader: ClassLoader?) {
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", loader, "getInstalledApplications", Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val applicationInfoList = param.result as MutableList<ApplicationInfo>
                val removeList = applicationInfoList.filter { it.packageName.contains("com.rarnu") || it.packageName.contains("de.robv.android.xposed.installer") }
                applicationInfoList.removeAll(removeList)
            }
        })
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", loader, "getInstalledPackages", Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val packageInfoList = param.result as MutableList<PackageInfo>
                val removeList = packageInfoList.filter { it.packageName.contains("com.rarnu") || it.packageName.contains("de.robv.android.xposed.installer") }
                packageInfoList.removeAll(removeList)
            }
        })
    }

}