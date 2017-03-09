package com.rarnu.norevoke.xposed

class WechatVersion(pkgName: String, versionName: String) {

    var packageName = ""
    var packageNameBase = ""
    var recallClass = ""
    var recallMethod = ""
    var storageClass = ""
    var storageMethod = ""
    var SQLiteDatabaseClass = ""

    init {
        packageName = pkgName
        packageNameBase = packageName.substring(0, packageName.lastIndexOf("."))

        SQLiteDatabaseClass = "$packageNameBase.mmdb.database.SQLiteDatabase"
        if (versionName.contains("6.5.3")) set653()
        if (versionName.contains("6.5.4")) set654()
    }

    private fun set654() {
        recallClass = "$packageName.sdk.platformtools.bg"
        recallMethod = "q"

        storageClass = "$packageName.storage.t"
        storageMethod = "$packageName.bg.g"
    }

    private fun set653() {
        recallClass = "$packageName.sdk.platformtools.bf"
        recallMethod = "q"

        storageClass = "$packageName.storage.r"
        storageMethod = "$packageName.bg.g"
    }
}