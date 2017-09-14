package com.gh0u1l5.wechatmagician.util

import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.DexClass
import java.lang.reflect.Field

// PackageUtil is a helper object for static analysis
object PackageUtil {

    // getClassName parses the standard class name of the given DexClass.
    private fun getClassName(clazz: DexClass): String {
        return clazz.classType
                .replace('/', '.') // replace delimiters
                .drop(1) // drop leading 'L'
                .dropLast(1) //drop trailing ';'
    }

    // findClassesFromPackage returns a list of all the classes contained in the given package.
    fun findClassesFromPackage(apkFile: ApkFile, packageName: String, depth: Int = 0): List<DexClass> {
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
    fun findClassWithMethod(
            classLoader: ClassLoader, classes: List<DexClass>,
            returnType: Class<*>, methodName: String, vararg parameterTypes: Class<*>): String {
        for (clazz in classes) {
            try {
                val className = getClassName(clazz)
                val method = XposedHelpers.findMethodExact(className, classLoader, methodName, *parameterTypes)
                if (method.returnType == returnType) {
                    return className
                }
            } catch (_: XposedHelpers.ClassNotFoundError) {
                continue
            } catch (_: NoSuchMethodError) {
                continue
            }
        }
        XposedBridge.log("HOOK => Cannot find class with method $methodName")
        return ""
    }

    // findFieldsWithGenericType finds all the fields of the given type
    fun findFieldsWithType(clazz: Class<*>, typeName: String): List<Field> {
        return clazz.fields.filter {
            it.type.name == typeName
        }
    }

    // findFieldsWithGenericType finds all the fields of the given generic type
    fun findFieldsWithGenericType(clazz: Class<*>, genericTypeName: String): List<Field> {
        return clazz.fields.filter {
            it.genericType.toString() == genericTypeName
        }
    }
}
