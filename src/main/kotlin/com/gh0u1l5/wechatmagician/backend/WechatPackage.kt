package com.gh0u1l5.wechatmagician.backend

import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Version
import com.gh0u1l5.wechatmagician.util.PackageUtil.findClassIfExists
import com.gh0u1l5.wechatmagician.util.PackageUtil.findClassesFromPackage
import com.gh0u1l5.wechatmagician.util.PackageUtil.findClassesWithSuper
import com.gh0u1l5.wechatmagician.util.PackageUtil.findFirstClassWithField
import com.gh0u1l5.wechatmagician.util.PackageUtil.findFirstClassWithMethod
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.DexClass

// WechatPackage analyzes and stores critical classes and objects in Wechat application.
// These classes and objects will be used for hooking and tampering with runtime data.
object WechatPackage {

    var XLogSetup: Class<*>? = null
    var SQLiteDatabaseClass: Class<*>? = null
    var EncEngine: Class<*>? = null
    var EncEngineEDMethod = ""

    var MMActivity: Class<*>? = null
    var MMFragmentActivity: Class<*>? = null
    var MMListPopupWindow: Class<*>? = null
    var PLTextView: Class<*>? = null

    var SnsUploadUI: Class<*>? = null
    var AdFrameLayout: Class<*>? = null
    var SnsPostTextView: Class<*>? = null
    var SnsPhotosContent: Class<*>? = null

    var AlbumPreviewUI: Class<*>? = null
    var SelectContactUI: Class<*>? = null
    var SelectConversationUI: Class<*>? = null
    var SelectConversationUIMaxLimitMethod = ""

    var MsgInfoClass: Class<*>? = null
    var ContactInfoClass: Class<*>? = null
    var MsgStorageClass: Class<*>? = null
    var MsgStorageInsertMethod = ""
    @Volatile var MsgStorageObject: Any? = null

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
    @Synchronized fun init(param: XC_LoadPackage.LoadPackageParam) {
        val loader = param.classLoader
        val version: Version
        val classes: Array<DexClass>

        var apkFile: ApkFile? = null
        try {
            apkFile = ApkFile(param.appInfo.sourceDir)
            version = Version(apkFile.apkMeta.versionName)
            classes = apkFile.dexClasses
        } finally {
            apkFile?.close()
        }

        XLogSetup = findClassIfExists("com.tencent.mm.xlog.app.XLogSetup", loader)
        SQLiteDatabaseClass = when {
            version >= Version("6.5.8") ->
                findClassIfExists("com.tencent.wcdb.database.SQLiteDatabase", loader)
            version >= Version("6.5.3") ->
                findClassIfExists("com.tencent.mmdb.database.SQLiteDatabase", loader)
            else -> null
        }
        EncEngine = findFirstClassWithMethod(
                findClassesFromPackage(loader, classes, "com.tencent.mm.modelsfs"),
                null, "seek", C.Long
        )
        EncEngineEDMethod = findMethodsByExactParameters(
                EncEngine, C.Int, C.ByteArray, C.Int
        ).firstOrNull()?.name ?: ""

        val pkgUI = "com.tencent.mm.ui"
        MMActivity = findClassIfExists("$pkgUI.MMActivity", loader)
        MMFragmentActivity = findClassIfExists("$pkgUI.MMFragmentActivity", loader)
        MMListPopupWindow = findClassIfExists("$pkgUI.base.MMListPopupWindow", loader)
        PLTextView = findClassIfExists("com.tencent.mm.kiss.widget.textview.PLSysTextView", loader)

        val pkgSnsUI = "com.tencent.mm.plugin.sns.ui"
        val snsUIClasses = findClassesFromPackage(loader, classes, pkgSnsUI)
        if (MMActivity != null) {
            SnsUploadUI = findFirstClassWithField(
                    findClassesWithSuper(snsUIClasses, MMActivity!!),
                    "$pkgSnsUI.SnsUploadSayFooter"
            )
        }
        AdFrameLayout = findClassIfExists("$pkgSnsUI.AdFrameLayout", loader)
        SnsPostTextView = findClassIfExists("$pkgSnsUI.widget.SnsPostDescPreloadTextView", loader)
        SnsPhotosContent = findClassIfExists("$pkgSnsUI.PhotosContent", loader)

        val pkgGalleryUI = "com.tencent.mm.plugin.gallery.ui"
        AlbumPreviewUI = findClassIfExists("$pkgGalleryUI.AlbumPreviewUI", loader)
        SelectContactUI = findClassIfExists("$pkgUI.contact.SelectContactUI", loader)
        SelectConversationUI = findClassIfExists("$pkgUI.transmit.SelectConversationUI", loader)
        SelectConversationUIMaxLimitMethod = findMethodsByExactParameters(
                SelectConversationUI, C.Boolean, C.Boolean
        ).firstOrNull()?.name ?: ""

        val storageClasses = findClassesFromPackage(loader, classes, "com.tencent.mm.storage")
        MsgInfoClass = findFirstClassWithMethod(storageClasses, C.Boolean, "isSystem")
        ContactInfoClass = findFirstClassWithMethod(storageClasses, C.String, "getCityCode")
        if (MsgInfoClass != null) {
            MsgStorageClass = findFirstClassWithMethod(
                    storageClasses, C.Long, MsgInfoClass!!, C.Boolean
            )
            MsgStorageInsertMethod = findMethodsByExactParameters(
                    MsgStorageClass, C.Long, MsgInfoClass!!, C.Boolean
            ).firstOrNull()?.name ?: ""
        }

        val platformClasses = findClassesFromPackage(loader, classes,"com.tencent.mm.sdk.platformtools")
        XMLParserClass = findFirstClassWithMethod(
                platformClasses, C.Map, C.String, C.String
        )
        XMLParseMethod = findMethodsByExactParameters(
                XMLParserClass, C.Map, C.String, C.String
        ).firstOrNull()?.name ?: ""

//        ImgStorageClass = findFirstClassWithMethod(
//                findClassesFromPackage(loader, apkFile, "com.tencent.mm", 1),
//                C.String, ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean
//        )
//        ImgStorageCacheField = findFieldsWithGenericType(
//                ImgStorageClass, "$CacheMapClass<java.lang.String, android.graphics.Bitmap>"
//        ).firstOrNull()?.name ?: ""
    }
}