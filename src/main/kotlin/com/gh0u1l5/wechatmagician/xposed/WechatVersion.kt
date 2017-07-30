package com.gh0u1l5.wechatmagician.xposed

import com.gh0u1l5.wechatmagician.util.Version

class WechatVersion(pkgName: String, versionStr: String) {

    var packageName = ""
    var packageNameBase = ""
    var recallClass = ""
    var recallMethod = ""
    var SQLiteDatabaseClass = ""

    init {
        packageName = pkgName
        packageNameBase = packageName.substring(0, packageName.lastIndexOf("."))

        val version = Version(versionStr)
        when {
            version >= Version("6.5.8") -> SQLiteDatabaseClass = "$packageNameBase.wcdb.database.SQLiteDatabase"
            version >= Version("6.5.3") -> SQLiteDatabaseClass = "$packageNameBase.mmdb.database.SQLiteDatabase"
            else -> throw Error("unsupported version")
        }

        when (version) {
            Version("6.5.3") -> {
                recallClass = "$packageName.sdk.platformtools.bg"
                recallMethod = "q"
            }
            Version("6.5.4") -> {
                recallClass = "$packageName.sdk.platformtools.bf"
                recallMethod = "q"
            }
        }
    }
}