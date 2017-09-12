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

    // TODO: Set class and method in WechatPackage
    private fun getThumbnailDirPath(): String {
        val clazz = findClass("com.tencent.mm.plugin.n.b", WechatHook.loader)
        return callStaticMethod(clazz, "xc") as String
    }

    private fun getImgIdFromPath(path: String): String {
        if (path.startsWith("THUMBNAIL_DIRPATH")) {
            return path.drop("THUMBNAIL_DIRPATH://th_".length)
        }
        return path
    }

    private fun getAbsolutePathFromImgId(imgId: String): String {
        val firstByte = imgId.take(2)
        val secondByte = imgId.drop(2).take(2)
        return "${getThumbnailDirPath()}$firstByte/$secondByte/th_$imgId"
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

    // TODO: Set class and method in WechatPackage
    private fun replaceThumbMemoryCache(path: String, bitmap: Bitmap) {
        val imgInfoStorageClass = findClass("com.tencent.mm.ah.n", WechatHook.loader)
        val imgInfoStorage = callStaticMethod(imgInfoStorageClass, "GT")

        // Update memory cache
        val cache = getObjectField(imgInfoStorage, "hFn")
        callMethod(cache, "remove", path)
        callMethod(cache, "k", "${path}hd", bitmap)

        // Notify storage update
        callMethod(imgInfoStorage, "doNotify")
    }

    fun replaceThumbnail(path: String, bitmap: Bitmap) {
        val imgId = getImgIdFromPath(path)
        val absolutePath = getAbsolutePathFromImgId(imgId)
        replaceThumbMemoryCache(absolutePath, bitmap)
        replaceThumbDiskCache("${absolutePath}hd", bitmap)
    }
}