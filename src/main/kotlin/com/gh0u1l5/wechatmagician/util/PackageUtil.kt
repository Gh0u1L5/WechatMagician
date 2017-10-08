package com.gh0u1l5.wechatmagician.util

import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.*
import net.dongliu.apk.parser.bean.DexClass
import java.lang.reflect.Field
import java.lang.reflect.Method

// PackageUtil is a helper object for static analysis
object PackageUtil {

    class Classes(private val classes: List<Class<*>>) {
        fun filterBySuper(superClass: Class<*>?): Classes {
            if (superClass == null) {
                return Classes(listOf())
            }
            return Classes(classes.filter { it.superclass == superClass })
        }

        fun filterByMethod(returnType: Class<*>?, methodName: String, vararg parameterTypes: Class<*>): Classes {
            return Classes(classes.filter {
                val method = PackageUtil.findMethodExactIfExists(it, methodName, *parameterTypes)
                method != null && method.returnType == returnType ?: method.returnType
            })
        }

        fun filterByMethod(returnType: Class<*>?, vararg parameterTypes: Class<*>): Classes {
            return Classes(classes.filter {
                findMethodsByExactParameters(it, returnType, *parameterTypes).isNotEmpty()
            })
        }

        fun filterByField(fieldType: String): Classes {
            return Classes(classes.filter {
                PackageUtil.findFieldsWithType(it, fieldType).isNotEmpty()
            })
        }

        fun firstOrNull(role: String, expectedSize: Int = 1): Class<*>? {
            if (classes.size != expectedSize) {
                log("APK PARSE => Expected to find $expectedSize class(es) for $role, found ${classes.size}")
                return null
            }
            return classes.firstOrNull()
        }
    }

    // shadowCopy copy all the fields of the object obj into the object copy.
    fun shadowCopy(obj: Any, copy: Any, clazz: Class<*>? = obj.javaClass) {
        if (clazz == null) {
            return
        }
        shadowCopy(obj, copy, clazz.superclass)
        clazz.declaredFields.forEach {
            it.isAccessible = true
            it.set(copy, it.get(obj))
        }
    }

    // findClassIfExists looks up and returns a class if it exists, otherwise it returns null.
    fun findClassIfExists(className:String, classLoader: ClassLoader): Class<*>? {
        return try { findClass(className, classLoader) } catch (_: Throwable) { null }
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
            loader: ClassLoader, classes: Array<DexClass>, packageName: String, depth: Int = 0
    ): Classes {
        return Classes(classes.filter predicate@ {
            if (depth == 0) {
                return@predicate it.packageName == packageName
            }
            val satisfyPrefix = it.packageName.startsWith(packageName)
            val satisfyDepth =
                    it.packageName.drop(packageName.length).count{it == '.'} == depth
            return@predicate satisfyPrefix && satisfyDepth
        }.map { findClass(getClassName(it), loader)!! })
    }

    // findMethodExactIfExists looks up and returns a method if it exists, otherwise it returns null.
    fun findMethodExactIfExists(
            clazz: Class<*>?, methodName: String, vararg parameterTypes: Class<*>
    ): Method? {
        return try { findMethodExact(clazz, methodName, *parameterTypes) } catch (_: Throwable) { null }
    }

    // findMethodsByExactParameters returns a list of all methods declared/overridden in a class with the specified parameter types.
    fun findMethodsByExactParameters(
            clazz: Class<*>?, returnType: Class<*>?, vararg parameterTypes: Class<*>
    ): List<Method> {
        if (clazz == null) {
            return listOf()
        }
        return XposedHelpers.findMethodsByExactParameters(
                clazz, returnType, *parameterTypes
        ).toList()
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
}
