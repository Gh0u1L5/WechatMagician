package com.gh0u1l5.wechatmagician.spellbook

import android.widget.Adapter
import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.BuildConfig
import com.gh0u1l5.wechatmagician.spellbook.SpellBook.getApplicationVersion
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryAsynchronously
import com.gh0u1l5.wechatmagician.spellbook.util.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassIfExists
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassesFromPackage
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findFieldsWithGenericType
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findFieldsWithType
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findMethodsByExactParameters
import com.gh0u1l5.wechatmagician.spellbook.util.Version
import com.gh0u1l5.wechatmagician.spellbook.util.WaitChannel
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.dongliu.apk.parser.ApkFile
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * This singleton is the core part that analyzes and stores critical classes and objects of Wechat.
 * These classes and objects will be used for hooking and tampering with runtime data.
 */
object WechatPackage {

    /**
     * A [WaitChannel] blocking all the evaluations until [WechatPackage.init] has finished.
     */
    private val initializeChannel = WaitChannel()

    /**
     * A [Version] holding the version of current Wechat.
     */
    @Volatile var version: Version? = null
    /**
     * A string holding the package name of current Wechat process.
     */
    @Volatile private var packageName: String = ""
    /**
     * A class loader holding the classes provided by the Wechat APK.
     */
    @Volatile var loader: ClassLoader? = null
    /**
     * A list holding a cache of full names for classes provided by the Wechat APK.
     */
    @Volatile private var classes: List<String>? = null

    // These are the cache of important global objects
    @Volatile var AddressAdapterObject: WeakReference<BaseAdapter?> = WeakReference(null)
    @Volatile var ConversationAdapterObject: WeakReference<BaseAdapter?> = WeakReference(null)
    @Volatile var SnsUserUIAdapterObject: WeakReference<Adapter?> = WeakReference(null)
    @Volatile var MsgStorageObject: Any? = null
    @Volatile var ImgStorageObject: Any? = null
    @Volatile var MainDatabaseObject: Any? = null
    @Volatile var SnsDatabaseObject: Any? = null

    /**
     * Creates a lazy object for inner usage in [WechatPackage]. Its evaluation will be blocked by
     * the [initializeChannel] if the initialization is unfinished.
     *
     * @param name The name of the lazy field. This is used to print a helpful error message.
     * @param initializer The callback that actually initialize the lazy object.
     * @return a lazy object that can be used for lazy evaluation.
     */
    private fun <T> innerLazy(name: String, initializer: () -> T?): Lazy<T> = lazy {
        initializeChannel.wait(8000)
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
    val WXCustomScheme_entry: Method by innerLazy("WXCustomScheme_entry") {
        findMethodsByExactParameters(WXCustomScheme, C.Boolean, C.Intent).firstOrNull()
    }

    val EncEngine: Class<*> by innerLazy("EncEngine") {
        findClassesFromPackage(loader!!, classes!!, "$packageName.modelsfs")
                .filterByMethod(null, "seek", C.Long)
                .filterByMethod(null, "free")
                .firstOrNull()
    }
    val EncEngine_transFor: Method by innerLazy("EncEngine_transFor") {
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
    val NotificationAppMsgQueue_add: Method by innerLazy("NotificationAppMsgQueue_add") {
        findMethodsByExactParameters(NotificationAppMsgQueue, null, NotificationItem).firstOrNull()
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
    val MMBaseAdapter_getItemInternal: String by innerLazy("MMBaseAdapter_getItemInternal") {
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
    val SnsUploadUI_mSnsEditText: Field by innerLazy("SnsUploadUI_mSnsEditText") {
        findFieldsWithType(SnsUploadUI, "$WECHAT_PACKAGE_SNS_UI.SnsEditText")
                .firstOrNull()?.apply { isAccessible = true }
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
    val SelectConversationUI_checkLimit: Method by innerLazy("SelectConversationUI_checkLimit") {
        findMethodsByExactParameters(SelectConversationUI, C.Boolean, C.Boolean).firstOrNull()
    }

    val MsgInfo: Class<*> by innerLazy("MsgInfo") {
        findClassesFromPackage(loader!!, classes!!, "$packageName.storage")
                .filterByMethod(C.Boolean, "isSystem")
                .firstOrNull()
    }
    val ContactInfo: Class<*> by innerLazy("ContactInfo") {
        findClassesFromPackage(loader!!, classes!!, "$packageName.storage")
                .filterByMethod(C.String, "getCityCode")
                .filterByMethod(C.String, "getCountryCode")
                .firstOrNull()
    }

    val MsgInfoStorage: Class<*> by innerLazy("MsgInfoStorage") {
        when {
            version!! >= Version("6.5.8") ->
                findClassesFromPackage(loader!!, classes!!, "$packageName.storage")
                        .filterByMethod(C.Long, MsgInfo, C.Boolean)
                        .firstOrNull()
            else ->
                findClassesFromPackage(loader!!, classes!!, "$packageName.storage")
                        .filterByMethod(C.Long, MsgInfo)
                        .firstOrNull()
        }
    }
    val MsgInfoStorage_insert: Method by innerLazy("MsgInfoStorage_insert") {
        when {
            version!! >= Version("6.5.8") ->
                findMethodsByExactParameters(MsgInfoStorage, C.Long, MsgInfo, C.Boolean).firstOrNull()
            else ->
                findMethodsByExactParameters(MsgInfoStorage, C.Long, MsgInfo).firstOrNull()
        }
    }

    val LruCache: Class<*> by innerLazy("LruCache") {
        findClassesFromPackage(loader!!, classes!!, "$packageName.sdk.platformtools")
                .filterByMethod(null, "trimToSize", C.Int)
                .firstOrNull()
    }

    val LruCacheWithListener: Class<*> by innerLazy("LruCacheWithListener") {
        findClassesFromPackage(loader!!, classes!!, packageName, 1)
                .filterBySuper(LruCache)
                .firstOrNull()
    }
    val LruCacheWithListener_put: Method by innerLazy("LruCacheWithListener_put") {
        findMethodsByExactParameters(LruCacheWithListener, null, C.Object, C.Object)
                .firstOrNull()?.apply { isAccessible = true }
    }

    val ImgInfoStorage: Class<*> by innerLazy("ImgInfoStorage") {
        findClassesFromPackage(loader!!, classes!!, packageName, 1)
                .filterByMethod(C.String, C.String, C.String, C.String, C.Boolean)
                .firstOrNull()
    }
    val ImgInfoStorage_mBitmapCache: Field by innerLazy("ImgInfoStorage_mBitmapCache") {
        findFieldsWithGenericType(
                ImgInfoStorage, "${LruCacheWithListener.canonicalName}<java.lang.String, android.graphics.Bitmap>")
                .firstOrNull()?.apply { isAccessible = true }
    }
    val ImgInfoStorage_load: Method by innerLazy("ImgInfoStorage_load") {
        findMethodsByExactParameters(ImgInfoStorage, C.String, C.String, C.String, C.String, C.Boolean)
                .firstOrNull()?.apply { isAccessible = true }
    }

    val XmlParser: Class<*> by innerLazy("XmlParser") {
        findClassesFromPackage(loader!!, classes!!, "$packageName.sdk.platformtools")
                .filterByMethod(C.Map, C.String, C.String)
                .firstOrNull()
    }
    val XmlParser_parse: Method by innerLazy("XmlParser_parse") {
        findMethodsByExactParameters(XmlParser, C.Map, C.String, C.String).firstOrNull()
    }

    /**
     * Loads necessary information for static analysis into [WechatPackage].
     *
     * @param lpparam The LoadPackageParam object that describes the current process. It should be
     * the same one passed to [de.robv.android.xposed.IXposedHookLoadPackage.handleLoadPackage].
     */
    @JvmStatic fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        tryAsynchronously {
            if (initializeChannel.isDone()) {
                return@tryAsynchronously
            }

            var apkFile: ApkFile? = null
            try {
                packageName = lpparam.packageName
                version = getApplicationVersion(lpparam.packageName)
                loader = lpparam.classLoader
                apkFile = ApkFile(lpparam.appInfo.sourceDir)
                classes = apkFile.dexClasses.map { clazz ->
                    PackageUtil.getClassName(clazz)
                }
            } catch (t: Throwable) {
                if (BuildConfig.DEBUG) {
                    log(t)
                }
            } finally {
                initializeChannel.done()
                apkFile?.close()
            }
        }
    }

    /**
     * Generates a report for sending support emails.
     */
    override fun toString(): String {
        val body = try {
            this::class.java.declaredFields.filter { field ->
                when (field.name) {
                    "INSTANCE", "\$\$delegatedProperties",
                    "initializeChannel",
                    "version", "packageName", "loader", "classes",
                    "WECHAT_PACKAGE_SQLITE",
                    "WECHAT_PACKAGE_UI",
                    "WECHAT_PACKAGE_SNS_UI",
                    "WECHAT_PACKAGE_GALLERY_UI" -> false
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
${body.removeSuffix("\n")}
===================================================="""
    }
}