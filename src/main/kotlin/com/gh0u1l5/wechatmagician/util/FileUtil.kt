package com.gh0u1l5.wechatmagician.util

import android.graphics.Bitmap
import android.os.SystemClock.elapsedRealtime
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.System.currentTimeMillis

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

    // readBytesFromDisk returns all the bytes of a binary file.
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

    // dumpStatusToFile dumps status to a file once per boot.
    fun dumpStatusToFile(path: String, status: ByteArray) {
        val file = File(path)
        if (!file.exists()) {
            writeBytesToDisk(path, status)
            return
        }
        val bootAt = currentTimeMillis() - elapsedRealtime()
        val modifiedAt = file.lastModified()
        if (modifiedAt < bootAt) {
            writeBytesToDisk(path, status)
        }
    }
}
