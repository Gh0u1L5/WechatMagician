package com.gh0u1l5.wechatmagician.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock.elapsedRealtime
import java.io.*
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

    // writeObjectToDisk writes a serializable object to disk.
    fun writeObjectToDisk(path: String, obj: Serializable) {
        val out = ByteArrayOutputStream()
        ObjectOutputStream(out).apply {
            writeObject(obj); close()
        }
        writeBytesToDisk(path, out.toByteArray())
    }

    // readObjectFromDisk reads a serializable object from disk.
    fun readObjectFromDisk(path: String): Any {
        val bytes = readBytesFromDisk(path)
        val ins = ByteArrayInputStream(bytes)
        return ObjectInputStream(ins).use {
            val obj = it.readObject()
            it.close(); obj
        }
    }

    // writeBitmapToDisk writes the given bitmap to disk.
    fun writeBitmapToDisk(path: String, bitmap: Bitmap) {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        FileUtil.writeBytesToDisk(path, out.toByteArray())
    }

    // writeOnce ensures that the writeCallback will only be executed once for each boot.
    fun writeOnce(path: String, writeCallback: (String) -> Unit) {
        val file = File(path)
        if (!file.exists()) {
            writeCallback(path)
            return
        }
        val bootAt = currentTimeMillis() - elapsedRealtime()
        val modifiedAt = file.lastModified()
        if (modifiedAt < bootAt) {
            writeCallback(path)
        }
    }

    // notifyNewMediaFile notifies all the gallery apps that there is a new file to scan.
    fun notifyNewMediaFile(path: String, context: Context?) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        context?.sendBroadcast(intent.apply {
            data = Uri.fromFile(File(path))
        })
    }
}
