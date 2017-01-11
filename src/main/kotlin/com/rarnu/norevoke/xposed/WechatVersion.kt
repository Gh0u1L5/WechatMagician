package com.rarnu.norevoke.xposed

/**
 * Created by rarnu on 1/11/17.
 */
class WechatVersion {

    var packageNameBase = ""
    var recallClass = ""
    var packageName = ""
    var recallMethod = ""
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
        recallClass = "$packageName.sdk.platformtools.bf"
        recallMethod = "q"
        storageClass = "$packageName.storage.r"
        storageMethod = "$packageName.bg.g"
        SQLiteDatabaseClass = "$packageNameBase.mmdb.database.SQLiteDatabase"
    }

}