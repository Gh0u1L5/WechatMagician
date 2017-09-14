package com.gh0u1l5.wechatmagician.xposed

import com.gh0u1l5.wechatmagician.util.C
import com.gh0u1l5.wechatmagician.util.PackageUtil
import com.gh0u1l5.wechatmagician.util.Version
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.dongliu.apk.parser.ApkFile

// WechatPackage analyzes and stores critical classes and objects in Wechat application.
// These classes and objects will be used for hooking and tampering with runtime data.
class WechatPackage(param: XC_LoadPackage.LoadPackageParam) {

    var SQLiteDatabaseClass: String

    var XMLParserClass: String
    var XMLParseMethod: String

    var CacheMapClass: String
    var CacheMapPutMethod: String
    var CacheMapRemoveMethod: String

    @Volatile var ImgStorageObject: Any? = null
    var ImgStorageClass: String
    var ImgStorageCacheField: String
    var ImgStorageLoadMethod: String
    var ImgStorageNotifyMethod: String

    // Analyzes Wechat package statically for the name of classes.
    // WechatHook will do the runtime analysis and set the objects.
    init {
        val apkFile = ApkFile(param.appInfo.sourceDir)
        val version = Version(apkFile.apkMeta.versionName)

        SQLiteDatabaseClass = when {
            version >= Version("6.5.8") -> "com.tencent.wcdb.database.SQLiteDatabase"
            version >= Version("6.5.3") -> "com.tencent.mmdb.database.SQLiteDatabase"
            else -> throw Error("unsupported version")
        }

        XMLParseMethod = "q"
        XMLParserClass = PackageUtil.findClassWithMethod(
                param.classLoader,
                PackageUtil.findClassesFromPackage(apkFile,"com.tencent.mm.sdk.platformtools"),
                C.Map, XMLParseMethod, C.String , C.String
        )

        CacheMapClass =  "com.tencent.mm.a.f"
        CacheMapPutMethod = "k"
        CacheMapRemoveMethod = "remove"

        ImgStorageLoadMethod = "a"
        ImgStorageNotifyMethod = "doNotify"
        ImgStorageClass = PackageUtil.findClassWithMethod(
                param.classLoader,
                PackageUtil.findClassesFromPackage(apkFile, "com.tencent.mm", 1),
                C.String, ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean
        )
        ImgStorageCacheField = PackageUtil.findFieldsWithGenericType(
                param.classLoader, ImgStorageClass,
                "$CacheMapClass<java.lang.String, android.graphics.Bitmap>"
        ).firstOrNull()?.name ?: ""
    }
}