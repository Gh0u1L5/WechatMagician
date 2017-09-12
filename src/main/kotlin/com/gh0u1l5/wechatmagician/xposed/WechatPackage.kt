package com.gh0u1l5.wechatmagician.xposed

import com.gh0u1l5.wechatmagician.util.Version
import de.robv.android.xposed.XposedHelpers.findMethodExact
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.DexClass

class WechatPackage(param: XC_LoadPackage.LoadPackageParam) {

    var XMLParserClass = ""
    var XMLParseMethod = ""
    var SQLiteDatabaseClass = ""

    init {
        val apkFile = ApkFile(param.appInfo.sourceDir)
        val version = Version(apkFile.apkMeta.versionName)

        SQLiteDatabaseClass = when {
            version >= Version("6.5.8") -> "com.tencent.wcdb.database.SQLiteDatabase"
            version >= Version("6.5.3") -> "com.tencent.mmdb.database.SQLiteDatabase"
            else -> throw Error("unsupported version")
        }

        XMLParseMethod = when {
            version >= Version("6.5.3") -> "q"
            else -> throw Error("unsupported version")
        }
        XMLParserClass = findClassWithMethod(
                param.classLoader,
                findClassesFromPackage(apkFile,"com.tencent.mm.sdk.platformtools"),
                Map::class.java, XMLParseMethod, String::class.java, String::class.java
        )
    }

    // getClassName parses the standard class name of the given DexClass
    private fun getClassName(clazz: DexClass): String {
        return clazz.classType
                .replace('/', '.') // replace delimiters
                .drop(1) // drop leading 'L'
                .dropLast(1) //drop trailing ';'
    }

    // findClassesFromPackage returns a list of all the classes contained in the given package
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
        throw Error("unsupported version")
    }
}