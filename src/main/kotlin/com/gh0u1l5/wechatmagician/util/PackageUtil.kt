package com.gh0u1l5.wechatmagician.util

import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError
import de.robv.android.xposed.XposedHelpers.findMethodExact
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.DexClass
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*

// PackageUtil is a helper object for static analysis
object PackageUtil {

    // getClassName parses the standard class name of the given DexClass.
    private fun getClassName(clazz: DexClass): String {
        return clazz.classType
                .replace('/', '.') // replace delimiters
                .drop(1) // drop leading 'L'
                .dropLast(1) //drop trailing ';'
    }

    // findClass finds a class based on its class name.
    // This wraps XposedHelpers.findClass() in a Kotlin style.
    private fun findClass(className: String, loader: ClassLoader): Class<*>? {
        return try { XposedHelpers.findClass(className, loader) } catch (_: ClassNotFoundError) { null }
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
            classes: List<Class<*>>,
            returnType: Class<*>?, methodName: String, vararg parameterTypes: Class<*>
    ): String {
        for (clazz in classes) {
            try {
                val method = findMethodExact(clazz, methodName, *parameterTypes)
                if (method.returnType == returnType ?: method.returnType) {
                    return clazz.name
                }
            } catch (_: ClassNotFoundError) {
                continue
            } catch (_: NoSuchMethodError) {
                continue
            }
        }
        XposedBridge.log("HOOK => Cannot find class with method $methodName")
        return ""
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
    fun findFieldsWithType(className: String, loader: ClassLoader, typeName: String): List<Field> {
        val clazz = findClass(className, loader)
        return findFieldsWithType(clazz, typeName)
    }

    // findFieldsWithGenericType finds all the fields of the given generic type.
    fun findFieldsWithGenericType(clazz: Class<*>?, genericTypeName: String): List<Field> {
        return clazz?.declaredFields?.filter {
            it.genericType.toString() == genericTypeName
        } ?: listOf()
    }
    fun findFieldsWithGenericType(
            className: String, loader: ClassLoader,
            genericTypeName: String
    ): List<Field> {
        val clazz = findClass(className, loader)
        return findFieldsWithGenericType(clazz, genericTypeName)
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
    fun findMethodsWithTypes(
            className: String, loader: ClassLoader,
            returnType: Class<*>?, vararg parameterTypes: Class<*>
    ): List<Method> {
        val clazz = findClass(className, loader)
        return findMethodsWithTypes(clazz, returnType, *parameterTypes)
    }
}
