package com.gh0u1l5.wechatmagician.util

import com.gh0u1l5.wechatmagician.backend.storage.cache.SnsCache
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.modelsfs.Classes.EncEngine
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.modelsfs.Methods.EncEngine_transFor
import com.gh0u1l5.wechatmagician.spellbook.util.FileUtil
import de.robv.android.xposed.XposedHelpers
import java.net.URL

// DownloadUtil is a helper object for downloading contents from Tencent CDN servers.
object DownloadUtil {

    // downloadImage downloads and decrypts the images from Tencent CDN servers.
    fun downloadImage(path: String, media: SnsCache.SnsMedia) {
        if (media.type != "2") {
            throw Error("Unsupported media type: ${media.type}")
        }
        if (media.main == null) {
            throw Error("Media URL is missing.")
        }
        if (media.main.token == null || media.main.idx == null) {
            throw Error("Null token or idx.")
        }

        val url = "${media.main.url}?tp=wxpc&token=${media.main.token}&idx=${media.main.idx}"
        val content = URL(url).readBytes()
        if (content.isEmpty()) {
            throw Error("Failed to download $url")
        }

        val encEngine = XposedHelpers.newInstance(EncEngine, media.main.key)
        EncEngine_transFor.invoke(encEngine, content, content.size)
        XposedHelpers.callMethod(encEngine, "free")
        FileUtil.writeBytesToDisk(path, content)
    }

    // downloadVideo downloads the videos from Tencent CDN servers.
    fun downloadVideo(path: String, media: SnsCache.SnsMedia) {
        if (media.type != "6") {
            throw Error("Unsupported media type: ${media.type}")
        }
        if (media.main == null) {
            throw Error("Media URL is missing.")
        }

        val content = URL(media.main.url).readBytes()
        if (content.isEmpty()) {
            throw Error("Failed to download ${media.main.url}")
        }
        FileUtil.writeBytesToDisk(path, content)
        downloadThumb("$path.thumb", media.thumb)
    }

    // downloadThumb downloads the thumbs from Tencent CDS server.
    fun downloadThumb(path: String, mediaURL: SnsCache.SnsMediaURL?) {
        if (mediaURL == null) {
            throw Error("Thumb URL is missing")
        }

        val content = URL(mediaURL.url).readBytes()
        if (content.isEmpty()) {
            throw Error("Failed to download ${mediaURL.url}")
        }
        FileUtil.writeBytesToDisk(path, content)
    }
}