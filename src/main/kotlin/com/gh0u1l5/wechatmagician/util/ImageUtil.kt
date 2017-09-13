package com.gh0u1l5.wechatmagician.util

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import com.gh0u1l5.wechatmagician.xposed.WechatHook
import de.robv.android.xposed.XposedHelpers.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

object ImageUtil {

    var blockTable: Set<String> = setOf()

    private fun getPathFromImgId(imgId: String): String {
        val obj = WechatHook.pkg.ImgStorageObject
        val method = WechatHook.pkg.ImgStorageLoadMethod
        return callMethod(obj, method, imgId, "th_", "", false) as String
    }

    private fun replaceThumbDiskCache(path: String, bitmap: Bitmap, retry: Boolean = true) {
        val file = File(path)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            bitmap.compress(JPEG, 100, out)
            out.flush()
        } catch (_: FileNotFoundException) {
            if (!retry) {
                return
            }
            file.parentFile.mkdirs()
            replaceThumbDiskCache(path, bitmap, false)
        } catch (_: Throwable) {
            // do not care about other errors
        } finally {
            synchronized(blockTable) {
                blockTable += path
            }
            out?.close()
        }
    }

    private fun replaceThumbMemoryCache(path: String, bitmap: Bitmap) {
        // Check if memory cache established
        val cache = WechatHook.pkg.CacheMapObject ?: return

        // Update memory cache
        callMethod(cache, WechatHook.pkg.CacheMapRemoveMethod, path)
        callMethod(cache, WechatHook.pkg.CacheMapPutMethod, "${path}hd", bitmap)

        // Notify storage update
        callMethod(WechatHook.pkg.ImgStorageObject, WechatHook.pkg.ImgStorageNotifyMethod)
    }

    // replaceThumbnail replace the memory cache and disk cache of a specific thumbnail
    fun replaceThumbnail(imgId: String, bitmap: Bitmap) {
        val path = getPathFromImgId(imgId)
        replaceThumbDiskCache("${path}hd", bitmap)
        replaceThumbMemoryCache(path, bitmap)
    }
}