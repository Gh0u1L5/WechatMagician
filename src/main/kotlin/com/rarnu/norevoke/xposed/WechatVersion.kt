package com.rarnu.norevoke.xposed

/**
 * Created by rarnu on 1/11/17.
 */
class WechatVersion {

    var packageName = ""
    var packageNameBase = ""
    var recallClass = ""
    var recallMethod = ""
    var storageClass = ""
    var storageMethod = ""
    var SQLiteDatabaseClass = ""

    constructor(pkgName: String, versionName: String) {
        packageName = pkgName
        packageNameBase = packageName.substring(0, packageName.lastIndexOf("."))
        if (versionName.contains("6.5.3")) {
            set653()
        } else if (versionName.contains("6.5.4")) {
            set654()
        }
    }

    private fun set654() {
        // database
        SQLiteDatabaseClass = "$packageNameBase.mmdb.database.SQLiteDatabase"

        // chat
        recallClass = "$packageName.sdk.platformtools.bg"
        recallMethod = "q"

        storageClass = "$packageName.storage.t"
        storageMethod = "$packageName.bg.g"
    }

    private fun set653() {
        // database
        SQLiteDatabaseClass = "$packageNameBase.mmdb.database.SQLiteDatabase"

        // chat
        recallClass = "$packageName.sdk.platformtools.bf"
        recallMethod = "q"

        storageClass = "$packageName.storage.r"
        storageMethod = "$packageName.bg.g"
    }

}