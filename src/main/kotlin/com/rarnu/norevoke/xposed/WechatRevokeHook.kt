package com.rarnu.norevoke.xposed

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * Created by rarnu on 1/11/17.
 */
class WechatRevokeHook {

    var _v: WechatVersion? = null
    var _db: WechatDatabase? = null
    // var _snsdb: SnsDatabase? = null

    constructor(ver: WechatVersion) {
        _v = ver
    }

    fun hook(loader: ClassLoader?) {
        try {
            hookDatabase(loader)
        } catch (t: Throwable) {
        }
        try {
            hookRevoke(loader)
        } catch (t: Throwable) {
        }
//        try {
//            hookSns(loader)
//        } catch (t: Throwable) {
//        }
        try {
            hookApplicationPackageManager(loader)
        } catch (t: Throwable) {
        }
    }

    private fun hookRevoke(loader: ClassLoader?) {

        XposedHelpers.findAndHookMethod(_v?.recallClass, loader, _v?.recallMethod, String::class.java, String::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val m = param.result as MutableMap<String, String?>?
                if (m != null) {
                    val type = m[".sysmsg.\$type"]
                    if (type == "revokemsg") {
                        val msgSvrId = m[".sysmsg.revokemsg.newmsgid"]
                        val cur = _db?.getMessageViaId(msgSvrId)
                        if (cur == null || !cur.moveToFirst()) {
                            return
                        }
                        val content = cur.getString(cur.getColumnIndex("content"))
                        cur.close()
                        if (content.contains("<?xml")) {
                            m[".sysmsg.\$type"] = null
                        } else {
                            var replaceMsg = m[".sysmsg.revokemsg.replacemsg"]!!
                            replaceMsg = replaceMsg.substring(1)
                            replaceMsg = replaceMsg.substring(0, replaceMsg.indexOf("\""))
                            replaceMsg = "  $replaceMsg 试图撤回一条消息:  \n  $content  "
                            m[".sysmsg.revokemsg.replacemsg"] = replaceMsg
                        }
                        param.result = m
                    }
                }
            }
        })
    }

//    private fun hookSns(loader: ClassLoader?) {
//        XposedHelpers.findAndHookConstructor(_v?.snsClass, loader, _v?.snsConstructorParam, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun afterHookedMethod(param: MethodHookParam) {
//                if (_snsdb == null) {
//                    _snsdb = SnsDatabase(_v, param.args[0], loader)
//                }
//            }
//        })
//    }

    private fun hookDatabase(loader: ClassLoader?) {
        XposedHelpers.findAndHookConstructor(_v?.storageClass, loader, _v?.storageMethod, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (_db == null) {
                    try {
                        _db = WechatDatabase(param.args[0])
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