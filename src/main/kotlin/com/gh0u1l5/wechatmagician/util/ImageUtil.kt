package com.gh0u1l5.wechatmagician.util

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import com.gh0u1l5.wechatmagician.xposed.SnsCache
import com.gh0u1l5.wechatmagician.xposed.WechatPackage
import de.robv.android.xposed.XposedHelpers.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

// ImageUtil is a helper object for processing thumbnails.
object ImageUtil {

    // blockTable records all the thumbnail files changed by ImageUtil
    // In WechatHook.hookImgStorage, the module hooks FileOutputStream
    // to prevent anyone from overwriting these files.
    @Volatile var blockTable: Set<String> = setOf()

    // getPathFromImgId maps the given imgId to corresponding absolute path.
    private fun getPathFromImgId(imgId: String): String? {
        val storage = WechatPackage.ImgStorageObject ?: return null
        val load = WechatPackage.ImgStorageLoadMethod
        return callMethod(storage, load, imgId, "th_", "", false) as String
    }

    fun downloadImage(path: String, media: SnsCache.SnsMedia) {
        val content = URL(
                "${media.url}?tp=wxpc&token=${media.token}&idx=${media.idx}"
        ).readBytes()
        if (content.isEmpty()) {
            return
        }
        val encEngine = newInstance(WechatPackage.EncEngine, media.key)
        callMethod(encEngine, WechatPackage.EncEngineEDMethod, content, content.size)

        val out = FileOutputStream(path)
        out.write(content)
        out.close()
    }

    // writeBitmapToDisk writes the given bitmap to disk and returns the result.
    fun writeBitmapToDisk(path: String, bitmap: Bitmap, retry: Boolean = true): Boolean {
        val file = File(path)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            bitmap.compress(JPEG, 100, out)
            out.flush()
            return true
        } catch (_: FileNotFoundException) {
            if (!retry) {
                return false
            }
            file.parentFile.mkdirs()
            return writeBitmapToDisk(path, bitmap, false)
        } catch (_: Throwable) {
            return false
        } finally {
            out?.close()
        }
    }

    // replaceThumbDiskCache replaces the disk cache of a specific
    // thumbnail with the given bitmap.
    private fun replaceThumbDiskCache(path: String, bitmap: Bitmap) {
        if (writeBitmapToDisk(path, bitmap)) {
            // Update block table after successful write.
            synchronized(blockTable) {
                blockTable += path
            }
        }
    }

    // replaceThumbMemoryCache replaces the memory cache of a specific
    // thumbnail with the given bitmap.
    private fun replaceThumbMemoryCache(path: String, bitmap: Bitmap) {
        // Check if memory cache and image storage are established
        val storage = WechatPackage.ImgStorageObject ?: return
        val cache = getObjectField(storage, WechatPackage.ImgStorageCacheField)

        // Update memory cache
        callMethod(cache, "remove", path)
        callMethod(cache, WechatPackage.CacheMapPutMethod, "${path}hd", bitmap)

        // Notify storage update
        callMethod(storage, "doNotify")
    }

    // replaceThumbnail replaces the memory cache and disk cache of a
    // specific thumbnail with the given bitmap.
    fun replaceThumbnail(imgId: String, bitmap: Bitmap) {
        val path = getPathFromImgId(imgId) ?: return
        replaceThumbDiskCache("${path}hd", bitmap)
        replaceThumbMemoryCache(path, bitmap)
    }

    // drawView draws the content of a view to a bitmap.
    fun drawView(view: View): Bitmap {
        val width = view.width
        val height = view.height
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        b.eraseColor(Color.WHITE)
        view.draw(Canvas(b))
        return b
    }
}