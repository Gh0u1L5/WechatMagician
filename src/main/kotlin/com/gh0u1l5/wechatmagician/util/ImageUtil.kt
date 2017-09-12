package com.gh0u1l5.wechatmagician.util

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import com.gh0u1l5.wechatmagician.xposed.WechatHook
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.XposedBridge.log
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object ImageUtil {

    private fun getPathFromImgId(imgId: String): String {
        val obj = WechatHook.pkg.ImgStorageObject
        val method = WechatHook.pkg.ImgStorageLoadMethod
        return callMethod(obj, method, imgId, "th_", "", false) as String
    }

    private fun replaceThumbDiskCache(path: String, bitmap: Bitmap) {
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(path)
            bitmap.compress(JPEG, 100, out)
            out.flush()
        } catch (e: IOException) {
            log("FS => ${e.message}")
        } catch (e: FileNotFoundException) {
            log("FS => ${e.message}")
        } finally {
            out?.close()
        }
    }

    private fun replaceThumbMemoryCache(path: String, bitmap: Bitmap) {
        // Update memory cache
        val cache = WechatHook.pkg.CacheMapObject
        callMethod(cache, WechatHook.pkg.CacheMapRemoveMethod, path)
        callMethod(cache, WechatHook.pkg.CacheMapPutMethod, "${path}hd", bitmap)

        // Notify storage update
        callMethod(WechatHook.pkg.ImgStorageObject, WechatHook.pkg.ImgStorageNotifyMethod)
    }

    fun replaceThumbnail(imgId: String, bitmap: Bitmap) {
        val path = getPathFromImgId(imgId)
        replaceThumbMemoryCache(path, bitmap)
        replaceThumbDiskCache("${path}hd", bitmap)
    }
}