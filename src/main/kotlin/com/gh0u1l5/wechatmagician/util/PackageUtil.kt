package com.gh0u1l5.wechatmagician.util

import de.robv.android.xposed.XC_MethodHook
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

        fun filterByEnclosingClass(enclosingClass: Class<*>?): Classes {
            return Classes(classes.filter { it.enclosingClass == enclosingClass })
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

        fun filterByField(fieldName: String, fieldType: String): Classes {
            return Classes(classes.filter {
                val field = it.getField(fieldName)
                field != null && field.type.name == fieldType
            })
        }

        fun filterByField(fieldType: String): Classes {
            return Classes(classes.filter {
                PackageUtil.findFieldsWithType(it, fieldType).isNotEmpty()
            })
        }

        fun firstOrNull(): Class<*>? = classes.firstOrNull()
    }

    // classesCache stores the result of findClassesFromPackage to speed up next search.
    private val classesCache: MutableMap<Pair<String, Int>, Classes> = HashMap()

    // shadowCopy copy all the fields of the object obj into the object copy.
    @JvmStatic fun shadowCopy(obj: Any, copy: Any, clazz: Class<*>? = obj.javaClass) {
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
    @JvmStatic fun findClassIfExists(className:String, classLoader: ClassLoader?): Class<*>? =
            try { findClass(className, classLoader) } catch (_: Throwable) { null }

    // getClassName parses the standard class name of the given DexClass.
    @JvmStatic fun getClassName(clazz: DexClass): String {
        return clazz.classType
                .replace('/', '.') // replace delimiters
                .drop(1) // drop leading 'L'
                .dropLast(1) //drop trailing ';'
    }

    // getPackageName parses the package name of the given DexClass.
    // NOTE: Here we do not use DexClass.getPackageName because of Issue #22
    // Reference: https://github.com/Gh0u1L5/WechatMagician/issues/22
    @JvmStatic fun getPackageName(clazz: DexClass): String {
        val className = getClassName(clazz)
        val delimiter = className.lastIndexOf('.')
        if (delimiter == -1) {
            return ""
        }
        return className.substring(0, delimiter)
    }

    // findClassesFromPackage returns a list of all the classes contained in the given package.
    @JvmStatic fun findClassesFromPackage(
            loader: ClassLoader, classes: Array<DexClass>, packageName: String, depth: Int = 0
    ): Classes {
        if ((packageName to depth) in classesCache) {
            return classesCache[packageName to depth]!!
        }

        classesCache[packageName to depth] = Classes(classes.filter predicate@ { clazz ->
            val currentPackage = getPackageName(clazz)
            if (depth == 0) {
                return@predicate currentPackage == packageName
            }
            val satisfyPrefix = currentPackage.startsWith(packageName)
            val satisfyDepth = currentPackage.drop(packageName.length).count{it == '.'} == depth
            return@predicate satisfyPrefix && satisfyDepth
        }.mapNotNull { findClassIfExists(getClassName(it), loader) })
        return classesCache[packageName to depth]!!
    }

    // findMethodExactIfExists looks up and returns a method if it exists, otherwise it returns null.
    @JvmStatic fun findMethodExactIfExists(
            clazz: Class<*>?, methodName: String, vararg parameterTypes: Class<*>
    ): Method? =
            try { findMethodExact(clazz, methodName, *parameterTypes) } catch (_: Throwable) { null }

    // findMethodsByExactParameters returns a list of all methods declared/overridden in a class with the specified parameter types.
    @JvmStatic fun findMethodsByExactParameters(
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
    @JvmStatic fun findFieldsWithType(clazz: Class<*>?, typeName: String): List<Field> {
        return clazz?.declaredFields?.filter {
            it.type.name == typeName
        } ?: listOf()
    }

    // findFieldsWithGenericType finds all the fields of the given generic type.
    @JvmStatic fun findFieldsWithGenericType(clazz: Class<*>?, genericTypeName: String): List<Field> {
        return clazz?.declaredFields?.filter {
            it.genericType.toString() == genericTypeName
        } ?: listOf()
    }

    @JvmStatic fun findAndHookMethod(clazz: Class<*>?, method: Method?, callback: XC_MethodHook) {
        if (clazz == null) {
            log("findAndHookMethod: clazz should not be null")
            return
        }
        if (method == null) {
            log("findAndHookMethod: method should not be null")
            return
        }
        findAndHookMethod(clazz, method.name, *method.parameterTypes, callback)
    }
}
