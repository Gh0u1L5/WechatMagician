package com.gh0u1l5.wechatmagician.util

import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.SnsCache
import de.robv.android.xposed.XposedHelpers
import java.io.File
import java.io.FileOutputStream
import java.net.URL

// DownloadUtil is a helper object for downloading contents from Tencent CDN servers.
object DownloadUtil {

    // writeBytesToDisk writes the given bytes to specific path.
    private fun writeBytesToDisk(path: String, content: ByteArray) {
        val file = File(path)
        file.parentFile.mkdirs()
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            out.write(content)
        } finally {
            out?.close()
        }
    }

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

        val encEngine = XposedHelpers.newInstance(WechatPackage.EncEngine, media.main.key)
        XposedHelpers.callMethod(encEngine, WechatPackage.EncEngineEDMethod, content, content.size)
        XposedHelpers.callMethod(encEngine, "free")
        writeBytesToDisk(path, content)
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
        writeBytesToDisk(path, content)
        downloadVideoThumb("$path.thumb", media.thumb)
    }

    // downloadVideo downloads videos from Tencent CDS server.
    private fun downloadVideoThumb(path: String, mediaURL: SnsCache.SnsMediaURL?) {
        if (mediaURL == null) {
            throw Error("Thumb URL is missing")
        }

        val content = URL(mediaURL.url).readBytes()
        if (content.isEmpty()) {
            throw Error("Failed to download ${mediaURL.url}")
        }
        writeBytesToDisk(path, content)
    }
}