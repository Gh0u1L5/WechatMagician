package com.gh0u1l5.wechatmagician.xposed

import android.content.ContentValues
import android.content.res.XModuleResources
import android.graphics.BitmapFactory
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.util.ImageUtil
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.xposed.MessageCache.WechatMessage
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class WechatHook : IXposedHookZygoteInit, IXposedHookLoadPackage {

    companion object {
        lateinit var pkg: WechatPackage
        lateinit var res: XModuleResources
        lateinit var loader: ClassLoader
    }

    override fun initZygote(param: IXposedHookZygoteInit.StartupParam?) {
        res = XModuleResources.createInstance(param?.modulePath, null)
    }

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName != "com.tencent.mm") {
            return
        }

        pkg = WechatPackage(param)
        loader = param.classLoader
        try {
            hookXMLParse()
            hookDatabase()
        } catch(e: NoSuchMethodError) {
            when {
                e.message!!.contains(pkg.SQLiteDatabaseClass) -> {
                    log("NSME => ${pkg.SQLiteDatabaseClass}")
                    pkg.SQLiteDatabaseClass = ""
                }
                e.message!!.contains("${pkg.XMLParserClass}#${pkg.XMLParseMethod}") -> {
                    log("NSME => ${pkg.XMLParserClass}#${pkg.XMLParseMethod}")
                    pkg.XMLParserClass = ""; pkg.XMLParseMethod = ""
                }
                else -> throw e
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun hookXMLParse() {
        if (pkg.XMLParserClass == "" || pkg.XMLParseMethod == "") {
            return
        }

        findAndHookMethod(pkg.XMLParserClass, loader, pkg.XMLParseMethod, String::class.java, String::class.java, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val XML = param.args[0] as String?
//                val tag = param.args[1] as String?
//                log("XMLParser => XML = $XML, tag = $tag")
//            }

            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                param.result = (param.result as? MutableMap<String, String?>)?.apply {
                    if (this[".sysmsg.\$type"] != "revokemsg") {
                        return
                    }
                    val replacemsg = this[".sysmsg.revokemsg.replacemsg"]
                    this[".sysmsg.revokemsg.replacemsg"] = replacemsg?.let {
                        if (it.startsWith("你") || it.startsWith("you", true)) it
                        else MessageUtil.customize(it, res.getString(R.string.easter_egg))
                    }
                }
            }
        })
    }

    private fun hookDatabase() {
        if (pkg.SQLiteDatabaseClass == ""){
            return
        }

        findAndHookMethod(pkg.SQLiteDatabaseClass, loader, "insertWithOnConflict", String::class.java, String::class.java, ContentValues::class.java, Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val table = param.args[0] as String?
                val initialValues = param.args[2] as ContentValues?
//                log("DB => insert table = $table, initialValues = $initialValues")

                if (table == "message") {
                    initialValues?.apply {
                        if (!containsKey("type") || !containsKey("talker")) {
                            log("DB => skewed message $initialValues")
                            return
                        }
                        val msgId = this["msgId"] as Long
                        MessageCache[msgId] = WechatMessage(
                                this["type"] as Int,
                                this["talker"] as String,
                                this["content"] as String?,
                                this["imgPath"] as String?)
                    }
                }
            }
        })

        findAndHookMethod(pkg.SQLiteDatabaseClass, loader, "updateWithOnConflict", String::class.java, ContentValues::class.java, String::class.java, Array<String?>::class.java, Integer.TYPE, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val table = param.args[0] as String?
                val values = param.args[1] as ContentValues?
//                val whereClause = param.args[2] as String?
//                val whereArgs = param.args[3] as Array<*>?
//                log("DB => update table = $table, values = $values, whereClause = $whereClause, whereArgs = ${MessageUtil.argsToString(whereArgs)}")

                val label_deleted = res.getString(R.string.label_deleted)
                when (table) {
                    "message" -> values?.apply {
                        if (!containsKey("type") || this["type"] != 10000) {
                            return
                        }
                        val sysMsg = this["content"] as String
                        if (sysMsg.startsWith("你") || sysMsg.startsWith("you", true)) {
                            return
                        }
                        remove("content"); remove("type")
                        MessageCache[this["msgId"] as Long]?.let {
                            handleMessageRecall(it, values)
                        }
                    }
                    "SnsInfo" -> values?.apply {
                        if (!containsKey("sourceType") || this["sourceType"] != 0) {
                            return
                        }
                        remove("sourceType")
                        val content =  this["content"] as ByteArray
                        put("content", MessageUtil.notifyInfoDelete(label_deleted, content))
                    }
                    "SnsComment" -> values?.apply {
                        if (!containsKey("type") || this["type"] == 1) {
                            return
                        }
                        if (!containsKey("commentflag") || this["commentflag"] != 1) {
                            return
                        }
                        remove("commentflag")
                        val curActionBuf = this["curActionBuf"] as ByteArray
                        put("curActionBuf", MessageUtil.notifyCommentDelete(label_deleted, curActionBuf))
                    }
                }
            }
        })

//        findAndHookMethod(pkg.SQLiteDatabaseClass, loader, "delete", String::class.java, String::class.java, Array<String?>::class.java, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val table = param.args[0] as String?
//                val whereClause = param.args[1] as String?
//                val whereArgs = param.args[2] as Array<*>?
//                log("DB => delete table = $table, whereClause = $whereClause, whereArgs = ${MessageUtil.argsToString(whereArgs)}")
//            }
//        })

//        findAndHookMethod(pkg.SQLiteDatabaseClass, loader, "executeSql", String::class.java, Array<Any?>::class.java, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val sql = param.args[0] as String?
//                val bindArgs = param.args[1] as Array<*>?
//                log("DB => executeSql sql = $sql, bindArgs = ${MessageUtil.argsToString(bindArgs)}")
//            }
//        })
    }

    private fun handleMessageRecall(origin: WechatMessage, values: ContentValues?) {
        val label_recalled = res.getString(R.string.label_recalled)

        val speaker: String?; var message: String?
        if (origin.talker.contains("chatroom")) {
            val len = (origin.content?.indexOf(":\n") ?: 0) + 2
            speaker = origin.content?.take(len)
            message = origin.content?.drop(len)
        } else {
            speaker = ""; message = origin.content
        }

        when (origin.type) {
            1 -> {
                message = MessageUtil.notifyMessageRecall(label_recalled, message!!)
                values?.put("content", speaker + message)
            }
            3 -> {
                val imgName = "image_recall_${res.getString(R.string.language)}.jpg"
                val imgStream = res.assets.open(imgName)
                val bitmap = BitmapFactory.decodeStream(imgStream)
                ImageUtil.replaceThumbnail(origin.imgPath!!, bitmap)
            }
            49 -> {
                message = MessageUtil.notifyLinkRecall(label_recalled, message!!)
                values?.put("content", speaker + message)
            }
        }
    }
}