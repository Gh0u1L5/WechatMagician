package com.gh0u1l5.wechatmagician.util

import android.graphics.Bitmap
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.ImgStorageObject
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.Fields.ImgInfoStorage_mBitmapCache
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.Methods.ImgInfoStorage_load
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.Methods.LruCacheWithListener_put
import com.gh0u1l5.wechatmagician.spellbook.util.FileUtil
import com.gh0u1l5.wechatmagician.spellbook.util.FileUtil.createTimeTag
import de.robv.android.xposed.XposedHelpers.callMethod

// ImageUtil is a helper object for processing thumbnails.
object ImageUtil {

    // blockTable records all the thumbnail files changed by ImageUtil
    // In WechatHook.hookImageStorage, the module hooks FileOutputStream
    // to prevent anyone from overwriting these files.
    @Volatile var blockTable: Set<String> = setOf()

    // getPathFromImgId maps the given imgId to corresponding absolute path.
    private fun getPathFromImgId(imgId: String): String? {
        val storage = ImgStorageObject ?: return null
        return ImgInfoStorage_load.invoke(storage, imgId, "th_", "", false) as? String
    }

    // replaceThumbDiskCache replaces the disk cache of a specific
    // thumbnail with the given bitmap.
    private fun replaceThumbDiskCache(path: String, bitmap: Bitmap) {
        try {
            FileUtil.writeBitmapToDisk(path, bitmap)
        } catch (_: Throwable) {
            return
        }
        // Update block table after successful write.
        synchronized(blockTable) {
            blockTable += path
        }
    }

    // replaceThumbMemoryCache replaces the memory cache of a specific
    // thumbnail with the given bitmap.
    private fun replaceThumbMemoryCache(path: String, bitmap: Bitmap) {
        // Check if memory cache and image storage are established
        val storage = ImgStorageObject ?: return
        val cache = ImgInfoStorage_mBitmapCache.get(storage)

        // Update memory cache
        callMethod(cache, "remove", path)
        callMethod(cache, LruCacheWithListener_put.name, "${path}hd", bitmap)

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

    // createScreenshotPath generates a path for saving the new screenshot
    fun createScreenshotPath(): String {
        val state = Environment.getExternalStorageState()
        if (state != MEDIA_MOUNTED) {
            throw Error("SD card is not presented! (state: $state)")
        }
        val storage = Environment.getExternalStorageDirectory().absolutePath + "/WechatMagician"
        return "$storage/screenshot/SNS-${createTimeTag()}.jpg"
    }
}