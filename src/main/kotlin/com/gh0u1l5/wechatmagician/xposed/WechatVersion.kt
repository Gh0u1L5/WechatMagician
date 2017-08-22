package com.gh0u1l5.wechatmagician.xposed

import com.gh0u1l5.wechatmagician.util.Version

class WechatVersion(pkgName: String, versionStr: String) {

    private var packageName = ""
    private var packageNameBase = ""
    var recallClass = ""
    var recallMethod = ""
    var SQLiteDatabaseClass = ""

    init {
        packageName = pkgName
        packageNameBase = packageName.substring(0, packageName.lastIndexOf("."))

        val version = Version(versionStr)
        SQLiteDatabaseClass = when {
            version >= Version("6.5.8") -> "$packageNameBase.wcdb.database.SQLiteDatabase"
            version >= Version("6.5.3") -> "$packageNameBase.mmdb.database.SQLiteDatabase"
            else -> throw Error("unsupported version")
        }

        recallMethod = "q"
        recallClass = when {
            version >= Version("6.5.13") -> "$packageName.sdk.platformtools.bg"
            version >= Version("6.5.10") -> "$packageName.sdk.platformtools.bh"
            version >= Version("6.5.4")  -> "$packageName.sdk.platformtools.bg"
            version == Version("6.5.3")  -> "$packageName.sdk.platformtools.bf"
            else -> throw Error("unsupported version")
        }
    }
}