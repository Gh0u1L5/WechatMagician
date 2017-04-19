package com.rarnu.norevoke.xposed

class WechatVersion(pkgName: String, versionName: String) {

    var packageName = ""
    var packageNameBase = ""
    var recallClass = ""
    var recallMethod = ""
    var SQLiteDatabaseClass = ""

    init {
        packageName = pkgName
        packageNameBase = packageName.substring(0, packageName.lastIndexOf("."))

        SQLiteDatabaseClass = "$packageNameBase.mmdb.database.SQLiteDatabase"
        if (versionName.contains("6.5.3")) {
            recallClass = "$packageName.sdk.platformtools.bg"
            recallMethod = "q"
        }
        if (versionName.contains("6.5.4")) {
            recallClass = "$packageName.sdk.platformtools.bf"
            recallMethod = "q"
        }
    }
}