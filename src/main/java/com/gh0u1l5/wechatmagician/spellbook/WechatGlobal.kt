package com.gh0u1l5.wechatmagician.spellbook

import android.widget.Adapter
import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.BuildConfig
import com.gh0u1l5.wechatmagician.spellbook.SpellBook.getApplicationVersion
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryAsynchronously
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil
import com.gh0u1l5.wechatmagician.spellbook.base.Version
import com.gh0u1l5.wechatmagician.spellbook.base.WaitChannel
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.dongliu.apk.parser.ApkFile
import java.lang.ref.WeakReference

/**
 * This singleton is the core part that analyzes and stores critical classes and objects of Wechat.
 * These classes and objects will be used for hooking and tampering with runtime data.
 */
object WechatGlobal {

    /**
     * A [WaitChannel] blocking all the evaluations until [WechatGlobal.init] has finished.
     */
    private val initializeChannel = WaitChannel()

    /**
     * A [Version] holding the version of current Wechat.
     */
    @Volatile var wxVersion: Version? = null
    /**
     * A string holding the package name of current Wechat process.
     */
    @Volatile var wxPackageName: String = ""
    /**
     * A class loader holding the classes provided by the Wechat APK.
     */
    @Volatile var wxLoader: ClassLoader? = null
    /**
     * A list holding a cache of full names for classes provided by the Wechat APK.
     */
    @Volatile var wxClasses: List<String>? = null

    // These are the cache of important global objects
    @Volatile var AddressAdapterObject: WeakReference<BaseAdapter?> = WeakReference(null)
    @Volatile var ConversationAdapterObject: WeakReference<BaseAdapter?> = WeakReference(null)
    @Volatile var SnsUserUIAdapterObject: WeakReference<Adapter?> = WeakReference(null)
    @Volatile var MsgStorageObject: Any? = null
    @Volatile var ImgStorageObject: Any? = null
    @Volatile var MainDatabaseObject: Any? = null
    @Volatile var SnsDatabaseObject: Any? = null

    /**
     * Creates a lazy object for dynamic analyzing. Its evaluation will be blocked by
     * the [initializeChannel] if the initialization is unfinished.
     *
     * @param name The name of the lazy field. This is used to print a helpful error message.
     * @param initializer The callback that actually initialize the lazy object.
     * @return a lazy object that can be used for lazy evaluation.
     */
    fun <T> wxLazy(name: String, initializer: () -> T?): Lazy<T> = lazy {
        initializeChannel.wait(8000)
        initializer() ?: throw Error("Failed to evaluate $name")
    }

    /**
     * Loads necessary information for static analysis into [WechatGlobal].
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
                wxPackageName = lpparam.packageName
                wxVersion = getApplicationVersion(lpparam.packageName)
                wxLoader = lpparam.classLoader
                apkFile = ApkFile(lpparam.appInfo.sourceDir)
                wxClasses = apkFile.dexClasses.map { clazz ->
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

    // TODO: find a new way for generating reports.
}