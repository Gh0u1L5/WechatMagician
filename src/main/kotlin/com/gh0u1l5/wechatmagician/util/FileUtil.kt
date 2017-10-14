package com.gh0u1l5.wechatmagician.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

// FileUtil is a helper object for file I/O.
object FileUtil {
    // writeBytesToDisk writes the given bytes to specific path.
    fun writeBytesToDisk(path: String, content: ByteArray) {
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

    fun readBytesFromDisk(path: String): ByteArray {
        var ins: FileInputStream? = null
        try {
            ins = FileInputStream(path)
            return ins.readBytes()
        } finally {
            ins?.close()
        }
    }

    // writeBitmapToDisk writes the given bitmap to disk.
    fun writeBitmapToDisk(path: String, bitmap: Bitmap) {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        FileUtil.writeBytesToDisk(path, out.toByteArray())
    }
}
