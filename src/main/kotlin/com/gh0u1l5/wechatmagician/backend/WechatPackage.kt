@file:Suppress("MemberVisibilityCanPrivate")

package com.gh0u1l5.wechatmagician.backend

import android.content.Context
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.WECHAT_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Version
import com.gh0u1l5.wechatmagician.util.FileUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil.findClassIfExists
import com.gh0u1l5.wechatmagician.util.PackageUtil.findClassesFromPackage
import com.gh0u1l5.wechatmagician.util.PackageUtil.findFieldsWithGenericType
import com.gh0u1l5.wechatmagician.util.PackageUtil.findFieldsWithType
import com.gh0u1l5.wechatmagician.util.PackageUtil.findMethodsByExactParameters
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.lang.reflect.Method
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write

// WechatPackage analyzes and stores critical classes and objects in Wechat application.
// These classes and objects will be used for hooking and tampering with runtime data.
object WechatPackage {

    // isInitialized indicates whether the WechatPackage has been initialized.
    private val initializeChannel = java.lang.Object()
    @Volatile var isInitialized = false

    // status stores the working status of all the hooks.
    private val statusLock = ReentrantReadWriteLock()
    private val status: HashMap<String, Boolean> = hashMapOf()

    // These stores necessary information to match signatures.
    @Volatile var loader: ClassLoader? = null
    @Volatile var version: Version? = null
    @Volatile var classes: List<String>? = null

    fun <T> innerLazy(name: String, initializer: () -> T?): Lazy<T> = lazy {
        synchronized(initializeChannel) {
            if (!isInitialized) {
                initializeChannel.wait()
            }
        }
        if (loader == null || version == null || classes == null) {
            throw Error("Failed to evaluate $name: initialization failed.")
        }
        initializer() ?: throw Error("Failed to evaluate $name")
    }

    val WECHAT_PACKAGE_SQLITE: String by innerLazy("WECHAT_PACKAGE_SQLITE") {
        when {
            version!! >= Version("6.5.8") -> "com.tencent.wcdb"
            else -> "com.tencent.mmdb"
        }
    }
    val WECHAT_PACKAGE_UI: String         = "$WECHAT_PACKAGE_NAME.ui"
    val WECHAT_PACKAGE_SNS_UI: String     = "$WECHAT_PACKAGE_NAME.plugin.sns.ui"
    val WECHAT_PACKAGE_GALLERY_UI: String = "$WECHAT_PACKAGE_NAME.plugin.gallery.ui"

    val LogCat: Class<*> by innerLazy("LogCat") {
        findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_NAME.sdk.platformtools")
                .filterByEnclosingClass(null)
                .filterByMethod(C.Int, "getLogLevel")
                .firstOrNull()
    }
    val XLogSetup: Class<*> by innerLazy("XLogSetup") {
        findClassIfExists("$WECHAT_PACKAGE_NAME.xlog.app.XLogSetup", loader)
    }
    val WebWXLoginUI: Class<*> by innerLazy("WebWXLoginUI") {
        findClassIfExists("$WECHAT_PACKAGE_NAME.plugin.webwx.ui.ExtDeviceWXLoginUI", loader)
    }
    val RemittanceAdapter: Class<*> by innerLazy("RemittanceAdapter") {
        findClassIfExists("$WECHAT_PACKAGE_NAME.plugin.remittance.ui.RemittanceAdapterUI", loader)
    }
    val ActionBarEditText: Class<*> by innerLazy("ActionBarEditText") {
        findClassIfExists("$WECHAT_PACKAGE_NAME.ui.tools.ActionBarSearchView.ActionBarEditText", loader)
    }

    val WXCustomScheme: Class<*> by innerLazy("WXCustomScheme") {
        findClassIfExists("$WECHAT_PACKAGE_NAME.plugin.base.stub.WXCustomSchemeEntryActivity", loader)
    }
    val WXCustomSchemeEntryMethod: Method by innerLazy("WXCustomSchemeEntryMethod") {
        findMethodsByExactParameters(WXCustomScheme, C.Boolean, C.Intent).firstOrNull()
    }

    val EncEngine: Class<*> by innerLazy("EncEngine") {
        findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_NAME.modelsfs")
                .filterByMethod(null, "seek", C.Long)
                .filterByMethod(null, "free")
                .firstOrNull()
    }
    val EncEngineEDMethod: Method by innerLazy("EncEngineEDMethod") {
        findMethodsByExactParameters(EncEngine, C.Int, C.ByteArray, C.Int).firstOrNull()
    }

    val SQLiteDatabase: Class<*> by innerLazy("SQLiteDatabase") {
        findClassIfExists("$WECHAT_PACKAGE_SQLITE.database.SQLiteDatabase", loader)
    }
    val SQLiteCursorFactory: Class<*> by innerLazy("SQLiteCursorFactory") {
        findClassIfExists("$WECHAT_PACKAGE_SQLITE.database.SQLiteDatabase.CursorFactory", loader)
    }
    val SQLiteErrorHandler: Class<*> by innerLazy("SQLiteErrorHandler") {
        findClassIfExists("$WECHAT_PACKAGE_SQLITE.DatabaseErrorHandler", loader)
    }
    val SQLiteCancellationSignal: Class<*> by innerLazy("SQLiteCancellationSignal") {
        findClassIfExists("$WECHAT_PACKAGE_SQLITE.support.CancellationSignal", loader)
    }

    val LauncherUI: Class<*> by innerLazy("LauncherUI") {
        findClassIfExists("$WECHAT_PACKAGE_UI.LauncherUI", loader)
    }
    val MMActivity: Class<*> by innerLazy("MMActivity") {
        findClassIfExists("$WECHAT_PACKAGE_UI.MMActivity", loader)
    }
    val MMFragmentActivity: Class<*> by innerLazy("MMFragmentActivity") {
        findClassIfExists("$WECHAT_PACKAGE_UI.MMFragmentActivity", loader)
    }
    val MMListPopupWindow: Class<*> by innerLazy("MMListPopupWindow") {
        findClassIfExists("$WECHAT_PACKAGE_UI.base.MMListPopupWindow", loader)
    }

    val BaseAdapter: Class<*> by innerLazy("BaseAdapter") {
        findClassIfExists("android.widget.BaseAdapter", loader)
    }
    val HeaderViewListAdapter: Class<*> by innerLazy("HeaderViewListAdapter") {
        findClassIfExists("android.widget.HeaderViewListAdapter", loader)
    }
    val MMBaseAdapter: Class<*> by innerLazy("MMBaseAdapter") {
        val addressBase = AddressAdapter.superclass
        val conversationBase = ConversationWithCacheAdapter.superclass
        if (addressBase != conversationBase) {
            log("Unexpected base adapter: $addressBase and $conversationBase")
        }
        return@innerLazy addressBase
    }
    val MMBaseAdapterGetMethod: String by innerLazy("MMBaseAdapterGetMethod") {
        MMBaseAdapter.declaredMethods.filter {
            it.parameterTypes.size == 1 && it.parameterTypes[0] == C.Int
        }.firstOrNull {
            it.name != "getItem" && it.name != "getItemId"
        }?.name
    }
    val AddressAdapter: Class<*> by innerLazy("AddressAdapter") {
        findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_UI.contact")
                .filterByMethod(null, "pause")
                .firstOrNull()
    }
    val ConversationWithCacheAdapter: Class<*> by innerLazy("ConversationWithCacheAdapter") {
        findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_UI.conversation")
                .filterByMethod(null, "clearCache")
                .firstOrNull()
    }

    val AddressUI: Class<*> by innerLazy("AddressUI") {
        findClassIfExists("$WECHAT_PACKAGE_UI.contact.AddressUI.a", loader)
    }
    val ContactLongClickListener: Class<*> by innerLazy("ContactLongClickListener") {
        findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_UI.contact")
                .filterByEnclosingClass(AddressUI)
                .filterByMethod(C.Boolean, "onItemLongClick", C.AdapterView, C.View, C.Int, C.Long)
                .firstOrNull()
    }
    val MainUI: Class<*> by innerLazy("MainUI") {
        findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_UI.conversation")
                .filterByMethod(C.Int, "getLayoutId")
                .filterByMethod(null, "onConfigurationChanged", C.Configuration)
                .firstOrNull()
    }
    val ConversationLongClickListener: Class<*> by innerLazy("ConversationLongClickListener") {
        when {
            version!! >= Version("6.5.8") ->
                findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_UI.conversation")
                        .filterByMethod(null, "onCreateContextMenu", C.ContextMenu, C.View, C.ContextMenuInfo)
                        .filterByMethod(C.Boolean, "onItemLongClick", C.AdapterView, C.View, C.Int, C.Long)
                        .firstOrNull()
            else ->
                findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_UI.conversation")
                        .filterByEnclosingClass(MainUI)
                        .filterByMethod(C.Boolean, "onItemLongClick", C.AdapterView, C.View, C.Int, C.Long)
                        .firstOrNull()
        }
    }
    val ConversationCreateContextMenuListener: Class<*> by innerLazy("ConversationCreateContextMenuListener") {
        when {
            version!! >= Version("6.5.8") -> ConversationLongClickListener
            else -> MainUI
        }
    }
    val ChattingUI: Class<*> by innerLazy("ChattingUI") {
        findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_UI.chatting")
                .filterBySuper(MMFragmentActivity)
                .filterByMethod(null, "onRequestPermissionsResult", C.Int, C.StringArray, C.IntArray)
                .firstOrNull()
    }

    val SnsActivity: Class<*> by innerLazy("SnsActivity") {
        findClassesFromPackage(loader!!, classes!!, WECHAT_PACKAGE_SNS_UI)
                .filterByField("$WECHAT_PACKAGE_UI.base.MMPullDownView")
                .firstOrNull()
    }
    val SnsUploadUI: Class<*> by innerLazy("SnsUploadUI") {
        findClassesFromPackage(loader!!, classes!!, WECHAT_PACKAGE_SNS_UI)
                .filterByField("$WECHAT_PACKAGE_SNS_UI.LocationWidget")
                .filterByField("$WECHAT_PACKAGE_SNS_UI.SnsUploadSayFooter")
                .firstOrNull()
    }
    val SnsUploadUIEditTextField: String by innerLazy("SnsUploadUIEditTextField") {
        findFieldsWithType(
                SnsUploadUI, "$WECHAT_PACKAGE_SNS_UI.SnsEditText"
        ).firstOrNull()?.name ?: ""
    }
    val SnsUserUI: Class<*> by innerLazy("SnsUserUI") {
        findClassIfExists("$WECHAT_PACKAGE_SNS_UI.SnsUserUI", loader)
    }
    val SnsTimeLineUI: Class<*> by innerLazy("SnsTimeLineUI") {
        findClassesFromPackage(loader!!, classes!!, WECHAT_PACKAGE_SNS_UI)
                .filterByField("android.support.v7.app.ActionBar")
                .firstOrNull()
    }

    val AlbumPreviewUI: Class<*> by innerLazy("AlbumPreviewUI") {
        findClassIfExists("$WECHAT_PACKAGE_GALLERY_UI.AlbumPreviewUI", loader)
    }
    val SelectContactUI: Class<*> by innerLazy("SelectContactUI") {
        findClassIfExists("$WECHAT_PACKAGE_UI.contact.SelectContactUI", loader)
    }
    val SelectConversationUI: Class<*> by innerLazy("SelectConversationUI") {
        findClassIfExists("$WECHAT_PACKAGE_UI.transmit.SelectConversationUI", loader)
    }
    val SelectConversationUIMaxLimitMethod: Method by innerLazy("SelectConversationUIMaxLimitMethod") {
        findMethodsByExactParameters(SelectConversationUI, C.Boolean, C.Boolean).firstOrNull()
    }

    val MsgInfoClass: Class<*> by innerLazy("MsgInfoClass") {
        findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_NAME.storage")
                .filterByMethod(C.Boolean, "isSystem")
                .firstOrNull()
    }
    val ContactInfoClass: Class<*> by innerLazy("ContactInfoClass") {
        findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_NAME.storage")
                .filterByMethod(C.String, "getCityCode")
                .filterByMethod(C.String, "getCountryCode")
                .firstOrNull()
    }

    @Volatile var MsgStorageObject: Any? = null
    val MsgStorageClass: Class<*> by innerLazy("MsgStorageClass") {
        when {
            version!! >= Version("6.5.8") ->
                findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_NAME.storage")
                        .filterByMethod(C.Long, MsgInfoClass, C.Boolean)
                        .firstOrNull()
            else ->
                findClassesFromPackage(loader!!, classes!!, "$WECHAT_PACKAGE_NAME.storage")
                        .filterByMethod(C.Long, MsgInfoClass)
                        .firstOrNull()
        }
    }
    val MsgStorageInsertMethod: Method by innerLazy("MsgStorageInsertMethod") {
        when {
            version!! >= Version("6.5.8") ->
                findMethodsByExactParameters(
                        MsgStorageClass, C.Long, MsgInfoClass, C.Boolean
                ).firstOrNull()
            else ->
                findMethodsByExactParameters(
                        MsgStorageClass, C.Long, MsgInfoClass
                ).firstOrNull()
        }
    }

    val CacheMapClass = "$WECHAT_PACKAGE_NAME.a.f"
    val CacheMapPutMethod = "k"

    @Volatile var ImgStorageObject: Any? = null
    val ImgStorageClass: Class<*> by innerLazy("ImgStorageClass") {
        findClassesFromPackage(loader!!, classes!!, WECHAT_PACKAGE_NAME, 1)
                .filterByMethod(C.String, ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean)
                .firstOrNull()
    }
    val ImgStorageCacheField: String by innerLazy("ImgStorageCacheField") {
        findFieldsWithGenericType(
                ImgStorageClass, "$CacheMapClass<java.lang.String, android.graphics.Bitmap>"
        ).firstOrNull()?.name ?: ""
    }
    val ImgStorageLoadMethod = "a"

    val XMLParserClass: Class<*> by innerLazy("XMLParserClass") {
        findClassesFromPackage(loader!!, classes!!,"$WECHAT_PACKAGE_NAME.sdk.platformtools")
                .filterByMethod(C.Map, C.String, C.String)
                .firstOrNull()
    }
    val XMLParseMethod: Method by innerLazy("XMLParseMethod") {
        findMethodsByExactParameters(
                XMLParserClass, C.Map, C.String, C.String
        ).firstOrNull()
    }

    // init initializes necessary information for static analysis.
    fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        thread(start = true) {
            try {
                loader = lpparam.classLoader
                version = getVersion(lpparam)

                var apkFile: ApkFile? = null
                try {
                    apkFile = ApkFile(lpparam.appInfo.sourceDir)
                    classes = apkFile.dexClasses.map { clazz ->
                        PackageUtil.getClassName(clazz)
                    }
                } finally {
                    apkFile?.close()
                }
            } finally {
                synchronized(initializeChannel) {
                    isInitialized = true
                    initializeChannel.notifyAll()
                }
            }
        }.setUncaughtExceptionHandler { _, throwable ->
            log(throwable)
        }
    }

    // getVersion returns the version of current package / application
    private fun getVersion(lpparam: XC_LoadPackage.LoadPackageParam): Version {
        val activityThreadClass = findClass("android.app.ActivityThread", null)
        val activityThread = callStaticMethod(activityThreadClass, "currentActivityThread")
        val context = callMethod(activityThread, "getSystemContext") as Context?
        val versionName = context?.packageManager?.getPackageInfo(lpparam.packageName, 0)?.versionName
        return Version(versionName ?: throw Error("Cannot get Wechat version"))
    }

    // setStatus updates current status of the Wechat hooks.
    fun setStatus(key: String, value: Boolean) {
        statusLock.write {
            status[key] = value
        }
    }

    // writeStatus writes current status to the given path.
    fun writeStatus(path: String) {
        statusLock.read {
            try {
                FileUtil.writeOnce(path, {
                    FileUtil.writeObjectToDisk(it, status)
                })
                FileUtil.setWorldReadable(File(path))
            } catch (_: Throwable) {
                // Ignore this one
            }
        }
    }

    override fun toString(): String {
        return this.javaClass.declaredFields.filter {
            when(it.name) {
                "INSTANCE", "status", "statusLock" -> false
                else -> true
            }
        }.joinToString("\n") {
            it.isAccessible = true; "${it.name} = ${it.get(this)}"
        }
    }
}