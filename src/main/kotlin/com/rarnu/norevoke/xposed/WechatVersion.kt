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
    var snsCommentClass = ""
    var storageClass = ""
    var storageMethod = ""
    var SQLiteDatabaseClass = ""

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
        snsCommentClass = "$packageName.plugin.sns.storage.h"

        storageClass = "$packageName.storage.r"
        storageMethod = "$packageName.bg.g"

    }

}