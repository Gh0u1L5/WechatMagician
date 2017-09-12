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

    private fun getAbsolutePathFromImgId(path: String): String {
        val obj = WechatHook.pkg.ImgInfoStorage
        val method = WechatHook.pkg.ImgLoadMethod
        return callMethod(obj, method, path, "th_", "", false) as String
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
        callMethod(WechatHook.pkg.CacheMap, "remove", path)
        callMethod(WechatHook.pkg.CacheMap, "k", "${path}hd", bitmap)

        // Notify storage update
        callMethod(WechatHook.pkg.ImgInfoStorage, "doNotify")
    }

    fun replaceThumbnail(path: String, bitmap: Bitmap) {
        val absolutePath = getAbsolutePathFromImgId(path)
        replaceThumbMemoryCache(absolutePath, bitmap)
        replaceThumbDiskCache("${absolutePath}hd", bitmap)
    }
}