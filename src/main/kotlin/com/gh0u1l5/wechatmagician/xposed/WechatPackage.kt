package com.gh0u1l5.wechatmagician.xposed

import com.gh0u1l5.wechatmagician.util.C
import com.gh0u1l5.wechatmagician.util.PackageUtil
import com.gh0u1l5.wechatmagician.util.Version
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.dongliu.apk.parser.ApkFile
import java.lang.reflect.Method

// WechatPackage analyzes and stores critical classes and objects in Wechat application.
// These classes and objects will be used for hooking and tampering with runtime data.
object WechatPackage {

    var XLogSetup: Class<*>? = null
    var MMActivity: Class<*>? = null
    var MMFragmentActivity: Class<*>? = null
    var MMListPopupWindow: Class<*>? = null

    var AlbumPreviewUI: Class<*>? = null
    var SelectContactUI: Class<*>? = null
    var SelectConversationUI: Class<*>? = null
    var SelectConversationUIMaxLimitMethod = ""

    var MsgInfoClass: Class<*>? = null
    var ContactInfoClass: Class<*>? = null
    var MsgStorageClass: Class<*>? = null
    var MsgStorageInsertMethod = ""
    @Volatile var MsgStorageObject: Any? = null

    var SQLiteDatabaseClass: Class<*>? = null

    var XMLParserClass: Class<*>? = null
    var XMLParseMethod = ""

    val CacheMapClass = "com.tencent.mm.a.f"
    val CacheMapPutMethod = "k"

    var ImgStorageClass: Class<*>? = null
    var ImgStorageCacheField = ""
    val ImgStorageLoadMethod = "a"
    @Volatile var ImgStorageObject: Any? = null

    // Analyzes Wechat package statically for the name of classes.
    // WechatHook will do the runtime analysis and set the objects.
    fun init(param: XC_LoadPackage.LoadPackageParam) {
        var pair: Pair<Class<*>?, Method?>

        val loader = param.classLoader
        val apkFile = ApkFile(param.appInfo.sourceDir)
        val version = Version(apkFile.apkMeta.versionName)

        XLogSetup = findClassIfExists("com.tencent.mm.xlog.app.XLogSetup", loader)
        MMActivity = findClassIfExists("com.tencent.mm.ui.MMActivity", loader)
        MMFragmentActivity = findClassIfExists("com.tencent.mm.ui.MMFragmentActivity", loader)
        MMListPopupWindow = findClassIfExists("com.tencent.mm.ui.base.MMListPopupWindow", loader)

        AlbumPreviewUI = findClassIfExists("com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI", loader)
        SelectContactUI = findClassIfExists("com.tencent.mm.ui.contact.SelectContactUI", loader)
        SelectConversationUI = findClassIfExists("com.tencent.mm.ui.transmit.SelectConversationUI", loader)
        SelectConversationUIMaxLimitMethod = PackageUtil.findMethodsWithTypes(
                SelectConversationUI, C.Boolean, C.Boolean
        ).firstOrNull()?.name ?: ""

        val storageClasses = PackageUtil.findClassesFromPackage(
                loader, apkFile, "com.tencent.mm.storage"
        )
        MsgInfoClass = PackageUtil.findFirstClassWithMethod(
                storageClasses, C.Boolean, "isSystem"
        )
        ContactInfoClass = PackageUtil.findFirstClassWithMethod(
                storageClasses, C.String, "getCityCode"
        )
        if (MsgInfoClass != null) {
            pair = PackageUtil.findFirstClassWithMethod(
                    storageClasses, C.Long, MsgInfoClass!!, C.Boolean
            )
            MsgStorageClass = pair.first
            MsgStorageInsertMethod = pair.second?.name ?: ""
        }

        SQLiteDatabaseClass = when {
            version >= Version("6.5.8") ->
                findClassIfExists("com.tencent.wcdb.database.SQLiteDatabase", loader)
            version >= Version("6.5.3") ->
                findClassIfExists("com.tencent.mmdb.database.SQLiteDatabase", loader)
            else -> null
        }

        pair = PackageUtil.findFirstClassWithMethod(
                PackageUtil.findClassesFromPackage(
                        loader, apkFile,"com.tencent.mm.sdk.platformtools"),
                C.Map, C.String, C.String
        )
        XMLParserClass = pair.first
        XMLParseMethod = pair.second?.name ?: ""

//        ImgStorageClass = PackageUtil.findFirstClassWithMethod(
//                PackageUtil.findClassesFromPackage(
//                        loader, apkFile, "com.tencent.mm", 1),
//                C.String, ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean
//        )
//        ImgStorageCacheField = PackageUtil.findFieldsWithGenericType(
//                ImgStorageClass, "$CacheMapClass<java.lang.String, android.graphics.Bitmap>"
//        ).firstOrNull()?.name ?: ""
    }
}