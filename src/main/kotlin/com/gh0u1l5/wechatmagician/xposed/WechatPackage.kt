package com.gh0u1l5.wechatmagician.xposed

import com.gh0u1l5.wechatmagician.util.C
import com.gh0u1l5.wechatmagician.util.Version
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findMethodExact
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.DexClass

// WechatPackage analyzes and stores critical classes and objects in Wechat application.
// These classes and objects will be used for hooking and tampering with runtime data.
class WechatPackage(param: XC_LoadPackage.LoadPackageParam) {

    var SQLiteDatabaseClass: String

    var XMLParserClass: String
    var XMLParseMethod: String

    @Volatile var CacheMapObject: Any? = null
    var CacheMapClass: String
    var CacheMapPutMethod: String
    var CacheMapRemoveMethod: String

    @Volatile var ImgStorageObject: Any? = null
    var ImgStorageClass: String
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
        XMLParserClass = findClassWithMethod(
                param.classLoader,
                findClassesFromPackage(apkFile,"com.tencent.mm.sdk.platformtools"),
                C.Map, XMLParseMethod, C.String , C.String
        )

        CacheMapClass =  "com.tencent.mm.a.f"
        CacheMapPutMethod = "k"
        CacheMapRemoveMethod = "remove"

        ImgStorageLoadMethod = "a"
        ImgStorageNotifyMethod = "doNotify"
        ImgStorageClass = findClassWithMethod(
                param.classLoader,
                findClassesFromPackage(apkFile, "com.tencent.mm", 1),
                C.String, ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean
        )
    }

    // getClassName parses the standard class name of the given DexClass.
    private fun getClassName(clazz: DexClass): String {
        return clazz.classType
                .replace('/', '.') // replace delimiters
                .drop(1) // drop leading 'L'
                .dropLast(1) //drop trailing ';'
    }

    // findClassesFromPackage returns a list of all the classes contained in the given package.
    private fun findClassesFromPackage(apkFile: ApkFile, packageName: String, depth: Int = 0): List<DexClass> {
        return apkFile.dexClasses.filter predicate@ {
            if (depth == 0) {
               return@predicate it.packageName == packageName
            }
            val satisfyPrefix = it.packageName.startsWith(packageName)
            val satisfyDepth =
                    it.packageName.drop(packageName.length).count{it == '.'} == depth
            return@predicate satisfyPrefix && satisfyDepth
        }
    }

    // findClassWithMethod finds the class that have the given method from a list of classes.
    private fun findClassWithMethod(
            classLoader: ClassLoader, classes: List<DexClass>,
            returnType: Class<*>, methodName: String, vararg parameterTypes: Class<*>): String {
        for (clazz in classes) {
            try {
                val className = getClassName(clazz)
                val method = findMethodExact(className, classLoader, methodName, *parameterTypes)
                if (method.returnType == returnType) {
                    return className
                }
            } catch (_: ClassNotFoundError) {
                continue
            } catch (_: NoSuchMethodError) {
                continue
            }
        }
        log("HOOK => Cannot find class with method $methodName")
        return ""
    }
}