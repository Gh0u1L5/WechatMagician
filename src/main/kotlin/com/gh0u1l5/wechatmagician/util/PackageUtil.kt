package com.gh0u1l5.wechatmagician.util

import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.*
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.DexClass
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*

// PackageUtil is a helper object for static analysis
object PackageUtil {

    fun deepCopy(obj: Any, copy: Any, clazz: Class<*>? = obj.javaClass) {
        if (clazz == null) {
            return
        }
        deepCopy(obj, copy, clazz.superclass)
        clazz.declaredFields.forEach {
            it.isAccessible = true
            it.set(copy, it.get(obj))
        }
    }

    // getClassName parses the standard class name of the given DexClass.
    private fun getClassName(clazz: DexClass): String {
        return clazz.classType
                .replace('/', '.') // replace delimiters
                .drop(1) // drop leading 'L'
                .dropLast(1) //drop trailing ';'
    }

    // findClassesFromPackage returns a list of all the classes contained in the given package.
    fun findClassesFromPackage(
            loader: ClassLoader, apkFile: ApkFile, packageName: String, depth: Int = 0
    ): List<Class<*>> {
        return apkFile.dexClasses.filter predicate@ {
            if (depth == 0) {
                return@predicate it.packageName == packageName
            }
            val satisfyPrefix = it.packageName.startsWith(packageName)
            val satisfyDepth =
                    it.packageName.drop(packageName.length).count{it == '.'} == depth
            return@predicate satisfyPrefix && satisfyDepth
        }.map { findClass(getClassName(it), loader)!! }
    }

    // findFirstClassWithMethod finds the class that have the given method from a list of classes.
    fun findFirstClassWithMethod(
            classes: List<Class<*>>, returnType: Class<*>?, methodName: String, vararg parameterTypes: Class<*>
    ): Class<*>? {
        for (clazz in classes) {
            val method = findMethodExactIfExists(
                    clazz, methodName, *parameterTypes
            ) ?: continue
            if (method.returnType == returnType ?: method.returnType) {
                return clazz
            }
        }
        XposedBridge.log("HOOK => Cannot find class with method $returnType $methodName($parameterTypes)")
        return null
    }
    fun findFirstClassWithMethod(
            classes: List<Class<*>>, returnType: Class<*>?, vararg parameterTypes: Class<*>
    ): Pair<Class<*>?, Method?> {
        for (clazz in classes) {
            val method = XposedHelpers.findMethodsByExactParameters(
                    clazz, returnType, *parameterTypes
            ).firstOrNull() ?: continue
            if (method.returnType == returnType ?: method.returnType) {
                return clazz to method
            }
        }
        XposedBridge.log("HOOK => Cannot find class with method signature $returnType fun($parameterTypes)")
        return null to null
    }

    // findClassesWithSuper finds the classes that have the given super class.
    fun findClassesWithSuper(classes: List<Class<*>>, superClass: Class<*>): List<Class<*>> {
        return classes.filter { it.superclass == superClass }
    }

    // findFieldsWithGenericType finds all the fields of the given type.
    fun findFieldsWithType(clazz: Class<*>?, typeName: String): List<Field> {
        return clazz?.declaredFields?.filter {
            it.type.name == typeName
        } ?: listOf()
    }

    // findFieldsWithGenericType finds all the fields of the given generic type.
    fun findFieldsWithGenericType(clazz: Class<*>?, genericTypeName: String): List<Field> {
        return clazz?.declaredFields?.filter {
            it.genericType.toString() == genericTypeName
        } ?: listOf()
    }

    // findMethodsWithTypes finds all the methods with the given return type and parameter types.
    fun findMethodsWithTypes(
            clazz: Class<*>?, returnType: Class<*>?, vararg parameterTypes: Class<*>
    ): List<Method> {
        return clazz?.declaredMethods?.filter { // Xposed Framework can only hook declared methods
            val satisfyReturn = it.returnType == returnType ?: it.returnType
            val satisfyParams = Arrays.equals(it.parameterTypes, parameterTypes)
            satisfyReturn && satisfyParams
        } ?: listOf()
    }
}
