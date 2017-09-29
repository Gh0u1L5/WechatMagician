package com.gh0u1l5.wechatmagician.xposed

import com.gh0u1l5.wechatmagician.util.C
import com.gh0u1l5.wechatmagician.util.PackageUtil
import com.gh0u1l5.wechatmagician.util.Version
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.dongliu.apk.parser.ApkFile

// WechatPackage analyzes and stores critical classes and objects in Wechat application.
// These classes and objects will be used for hooking and tampering with runtime data.
object WechatPackage {

    val MMActivity = "com.tencent.mm.ui.MMActivity"
    val MMFragmentActivity = "com.tencent.mm.ui.MMFragmentActivity"

    var AlbumPreviewUI = "com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI"
    var SelectContactUI = "com.tencent.mm.ui.contact.SelectContactUI"
    var SelectConversationUI = "com.tencent.mm.ui.transmit.SelectConversationUI"
    var SelectConversationUIMaxLimitMethod = ""

    @Volatile var MsgStorageObject: Any? = null
    var MsgInfoClass = ""
    var MsgStorageClass = ""
    var MsgStorageInsertMethod = ""

    var ContactInfoClass = ""
    var SQLiteDatabaseClass = ""

    var XMLParserClass = ""
    var XMLParseMethod = ""

    private val CacheMapClass = "com.tencent.mm.a.f"
    val CacheMapPutMethod = "k"
    val CacheMapRemoveMethod = "remove"

    @Volatile var ImgStorageObject: Any? = null
    var ImgStorageClass = ""
    var ImgStorageCacheField = ""
    val ImgStorageLoadMethod = "a"
    val ImgStorageNotifyMethod = "doNotify"

    // Analyzes Wechat package statically for the name of classes.
    // WechatHook will do the runtime analysis and set the objects.
    fun init(param: XC_LoadPackage.LoadPackageParam) {
        var pair: Pair<String, String>

        val apkFile = ApkFile(param.appInfo.sourceDir)
        val version = Version(apkFile.apkMeta.versionName)

        SelectConversationUIMaxLimitMethod = PackageUtil.findMethodsWithTypes(
                SelectConversationUI, param.classLoader,
                C.Boolean, C.Boolean
        ).firstOrNull()?.name ?: ""

        val storageClasses = PackageUtil.findClassesFromPackage(
                param.classLoader, apkFile, "com.tencent.mm.storage"
        )
        MsgInfoClass = PackageUtil.findFirstClassWithMethod(
                storageClasses, C.Boolean, "isSystem"
        )
        pair = PackageUtil.findFirstClassWithMethod(
                storageClasses, C.Long, findClass(MsgInfoClass, param.classLoader), C.Boolean
        )
        MsgStorageClass = pair.first
        MsgStorageInsertMethod = pair.second

        ContactInfoClass = PackageUtil.findFirstClassWithMethod(
                PackageUtil.findClassesFromPackage(
                        param.classLoader, apkFile, "com.tencent.mm.storage"),
                C.String, "getCityCode"
        )

        SQLiteDatabaseClass = when {
            version >= Version("6.5.8") -> "com.tencent.wcdb.database.SQLiteDatabase"
            version >= Version("6.5.3") -> "com.tencent.mmdb.database.SQLiteDatabase"
            else -> throw Error("unsupported version")
        }

        pair = PackageUtil.findFirstClassWithMethod(
                PackageUtil.findClassesFromPackage(
                        param.classLoader, apkFile,"com.tencent.mm.sdk.platformtools"),
                C.Map, C.String , C.String
        )
        XMLParserClass = pair.first
        XMLParseMethod = pair.second

//        ImgStorageClass = PackageUtil.findFirstClassWithMethod(
//                PackageUtil.findClassesFromPackage(
//                        param.classLoader, apkFile, "com.tencent.mm", 1),
//                C.String, ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean
//        )
//        ImgStorageCacheField = PackageUtil.findFieldsWithGenericType(
//                ImgStorageClass, param.classLoader,
//                "$CacheMapClass<java.lang.String, android.graphics.Bitmap>"
//        ).firstOrNull()?.name ?: ""
    }
}