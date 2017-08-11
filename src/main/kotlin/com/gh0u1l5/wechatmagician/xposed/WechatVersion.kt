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

        recallMethod = "q"
        when {
            version >= Version("6.5.10") -> {
                recallClass = "$packageName.sdk.platformtools.bh"
            }
            version >= Version("6.5.4") -> {
                recallClass = "$packageName.sdk.platformtools.bg"
            }
            version == Version("6.5.3") -> {
                recallClass = "$packageName.sdk.platformtools.bf"
            }
            else -> throw Error("unsupported version")
        }
    }
}