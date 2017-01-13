package com.rarnu.norevoke.xposed

/**
 * Created by rarnu on 1/11/17.
 */
class WechatVersion {

    var packageName = ""
    var packageNameBase = ""
    var recallClass = ""
    var recallMethod = ""
    var snsClass = ""
    var snsConstructorParam = ""
    var storageClass = ""
    var storageMethod = ""
    var SQLiteDatabaseClass = ""
    // sns
    var blobDecodeMethod = ""
    var commentContentField = ""
    var snsContentField = ""
    var commentsListField = ""
    var commentTimeField = ""
    var commentClass = ""
    var snsContentClass = ""
    var snsAttrClass = ""

    constructor(pkgName: String, versionName: String) {
        packageName = pkgName
        packageNameBase = packageName.substring(0, packageName.lastIndexOf("."))
        if (versionName.contains("6.5.3")) {
            set653()
        }
    }

    private fun set653() {
        // database
        SQLiteDatabaseClass = "$packageNameBase.mmdb.database.SQLiteDatabase"

        // chat
        recallClass = "$packageName.sdk.platformtools.bf"
        recallMethod = "q"
        snsClass = "$packageName.plugin.sns.storage.l"
        snsConstructorParam = "$packageName.sdk.h.d"
        storageClass = "$packageName.storage.r"
        storageMethod = "$packageName.bg.g"

        // sns
        commentClass = "com.tencent.mm.protocal.c.avm"
        commentContentField = "hQP"
        commentsListField = "mSN"
        commentTimeField = "hNS"
        blobDecodeMethod = "az"
        snsContentClass = "com.tencent.mm.protocal.c.azr"
        snsContentField = "mWn"
        snsAttrClass = "com.tencent.mm.protocal.c.awa"

    }

}