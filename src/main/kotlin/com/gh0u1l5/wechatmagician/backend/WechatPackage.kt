package com.gh0u1l5.wechatmagician.backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Adapter
import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.BuildConfig
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_HOOK_STATUS
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_WECHAT_PACKAGE
import com.gh0u1l5.wechatmagician.Global.tryAsynchronously
import com.gh0u1l5.wechatmagician.Global.tryVerbosely
import com.gh0u1l5.wechatmagician.Version
import com.gh0u1l5.wechatmagician.WaitChannel
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
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

// WechatPackage analyzes and stores critical classes and objects in Wechat application.
// These classes and objects will be used for hooking and tampering with runtime data.
object WechatPackage {

    // initializeChannel resumes all the thread waiting for the WechatPackage initialization.
    private val initializeChannel = WaitChannel()

    // status stores the working status of all the hooks.
    private val status = ConcurrentHashMap<String, Boolean>()

    // These stores necessary information to match signatures.
    @Volatile var packageName: String = ""
    @Volatile var loader: ClassLoader? = null
    @Volatile var version: Version? = null
    @Volatile var classes: List<String>? = null

    // These are the cache of important global objects
    @Volatile var AddressAdapterObject: WeakReference<BaseAdapter?> = WeakReference(null)
    @Volatile var ConversationAdapterObject: WeakReference<BaseAdapter?> = WeakReference(null)
    @Volatile var SnsUserUIAdapterObject: WeakReference<Adapter?> = WeakReference(null)
    @Volatile var MsgStorageObject: Any? = null
    @Volatile var ImgStorageObject: Any? = null
    @Volatile var MainDatabaseObject: Any? = null
    @Volatile var SnsDatabaseObject: Any? = null

    private fun <T> innerLazy(name: String, initializer: () -> T?): Lazy<T> = lazy {
        initializeChannel.wait()
        initializer() ?: throw Error("Failed to evaluate $name")
    }

    private val WECHAT_PACKAGE_SQLITE: String by innerLazy("WECHAT_PACKAGE_SQLITE") {
        when {
            version!! >= Version("6.5.8") -> "com.tencent.wcdb"
            else -> "com.tencent.mmdb"
        }
    }
    private val WECHAT_PACKAGE_UI: String         by lazy { "$packageName.ui" }
    private val WECHAT_PACKAGE_SNS_UI: String     by lazy { "$packageName.plugin.sns.ui" }
    private val WECHAT_PACKAGE_GALLERY_UI: String by lazy { "$packageName.plugin.gallery.ui" }

    val LogCat: Class<*> by innerLazy("LogCat") {
        findClassesFromPackage(loader!!, classes!!, "$packageName.sdk.platformtools")
                .filterByEnclosingClass(null)
                .filterByMethod(C.Int, "getLogLevel")
                .firstOrNull()
    }
    val WebWXLoginUI: Class<*> by innerLazy("WebWXLoginUI") {
        findClassIfExists("$packageName.plugin.webwx.ui.ExtDeviceWXLoginUI", loader)
    }
    val RemittanceAdapter: Class<*> by innerLazy("RemittanceAdapter") {
        findClassIfExists("$packageName.plugin.remittance.ui.RemittanceAdapterUI", loader)
    }
    val ActionBarEditText: Class<*> by innerLazy("ActionBarEditText") {
        findClassIfExists("$packageName.ui.tools.ActionBarSearchView.ActionBarEditText", loader)
    }

    val WXCustomScheme: Class<*> by innerLazy("WXCustomScheme") {
        findClassIfExists("$packageName.plugin.base.stub.WXCustomSchemeEntryActivity", loader)
    }
    val WXCustomSchemeEntryMethod: Method by innerLazy("WXCustomSchemeEntryMethod") {
        findMethodsByExactParameters(WXCustomScheme, C.Boolean, C.Intent).firstOrNull()
    }

    val EncEngine: Class<*> by innerLazy("EncEngine") {
        findClassesFromPackage(loader!!, classes!!, "$packageName.modelsfs")
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

    val NotificationItem: Class<*> by innerLazy("NotificationItem") {
        findClassIfExists("$packageName.booter.notification.NotificationItem", loader)
    }
    val NotificationAppMsgQueue: Class<*> by innerLazy("NotificationAppMsgQueue") {
        findClassesFromPackage(loader!!, classes!!, "$packageName.booter.notification.queue")
                .filterByMethod(null, NotificationItem)
                .firstOrNull()
    }
    val NotificationAppMsgQueueAddMethod: Method by innerLazy("NotificationAppMsgQueueAddMethod") {
        findMethodsByExactParameters(NotificationAppMsgQueue, NotificationItem).firstOrNull()
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
        findClassesFromPackage(loader!!, classes!!, "$packageName.storage")
                .filterByMethod(C.Boolean, "isSystem")
                .firstOrNull()
    }
    val ContactInfoClass: Class<*> by innerLazy("ContactInfoClass") {
        findClassesFromPackage(loader!!, classes!!, "$packageName.storage")
                .filterByMethod(C.String, "getCityCode")
                .filterByMethod(C.String, "getCountryCode")
                .firstOrNull()
    }

    val MsgStorageClass: Class<*> by innerLazy("MsgStorageClass") {
        when {
            version!! >= Version("6.5.8") ->
                findClassesFromPackage(loader!!, classes!!, "$packageName.storage")
                        .filterByMethod(C.Long, MsgInfoClass, C.Boolean)
                        .firstOrNull()
            else ->
                findClassesFromPackage(loader!!, classes!!, "$packageName.storage")
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

    val CacheMapClass: String by lazy { "$packageName.a.f" }
    val CacheMapPutMethod = "k"

    val ImgStorageClass: Class<*> by innerLazy("ImgStorageClass") {
        findClassesFromPackage(loader!!, classes!!, packageName, 1)
                .filterByMethod(C.String, ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean)
                .firstOrNull()
    }
    val ImgStorageCacheField: Field by innerLazy("ImgStorageCacheField") {
        findFieldsWithGenericType(
                ImgStorageClass, "$CacheMapClass<java.lang.String, android.graphics.Bitmap>"
        ).firstOrNull()?.apply{ isAccessible = true }
    }
    val ImgStorageLoadMethod = "a"

    val XMLParserClass: Class<*> by innerLazy("XMLParserClass") {
        findClassesFromPackage(loader!!, classes!!,"$packageName.sdk.platformtools")
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
        tryAsynchronously {
            try {
                packageName = lpparam.packageName
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
            } catch (t: Throwable) {
                // Ignore this one
            } finally {
                initializeChannel.done()
            }
        }
    }

    private val requireHookStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            setResultExtras(Bundle().apply {
                putSerializable("status", status)
            })
        }
    }

    private val requireWechatPackageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            resultData = this@WechatPackage.toString()
        }
    }

    // listen returns debug output to the frontend.
    fun listen(context: Context) {
        tryVerbosely {
            context.registerReceiver(requireHookStatusReceiver, IntentFilter(ACTION_REQUIRE_HOOK_STATUS))
            context.registerReceiver(requireWechatPackageReceiver, IntentFilter(ACTION_REQUIRE_WECHAT_PACKAGE))
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
    fun setStatus(key: String, value: Boolean) { status[key] = value }

    override fun toString(): String {
        val body = try {
            this.javaClass.declaredFields.filter { field ->
                when (field.name) {
                    "INSTANCE", "\$\$delegatedProperties",
                    "initializeChannel", "status",
                    "packageName", "loader", "version", "classes",
                    "WECHAT_PACKAGE_SQLITE",
                    "WECHAT_PACKAGE_UI",
                    "WECHAT_PACKAGE_SNS_UI",
                    "WECHAT_PACKAGE_GALLERY_UI",
                    "requireHookStatusReceiver",
                    "requireWechatPackageReceiver" -> false
                    else -> true
                }
            }.joinToString("\n") {
                it.isAccessible = true
                val key = it.name.removeSuffix("\$delegate")
                var value = it.get(this)
                if (value is WeakReference<*>) {
                    value = value.get()
                }
                "$key = $value"
            }
        } catch (t: Throwable) { "Error: " + t.localizedMessage }

        return """====================================================
Wechat Package: $packageName
Wechat Version: $version
Module Version: ${BuildConfig.VERSION_NAME}
${body.removeSuffix("\n")}
===================================================="""
    }
}