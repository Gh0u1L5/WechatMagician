package com.gh0u1l5.wechatmagician.util

import android.os.Bundle
import java.math.BigInteger

// MessageUtil is a helper object for processing Wechat internal strings / byte arrays.
object MessageUtil {

    fun applyEasterEgg(str: String, easter_egg: String): String {
        return str.let{
            "${it.take(it.indexOf("\"", 1) + 1)} $easter_egg"
        }
    }

    fun notifyInfoDelete(head: String?, msg: ByteArray?): ByteArray? {
        if (head == null || msg == null){
            return null
        }

        val start = msg.indexOf(0x2A) + 1
        val (lenSize, msgSize) = decodeMsgSize(start, msg)

        val content = msg.sliceArray(start+lenSize..msg.size)
        if (String(content).startsWith(head)){
            return msg
        }

        val len = encodeMsgSize("$head ".toByteArray().size + msgSize)
        return msg.copyOfRange(0, start) + len + "$head ".toByteArray() + msg.copyOfRange(start + lenSize, msg.size)
    }

    fun notifyCommentDelete(head: String?, msg: ByteArray?): ByteArray? {
        if (head == null || msg == null){
            return null
        }

        val namestart = msg.indexOf(0x22)
        val start = namestart + msg[namestart + 1].toInt() + 13
        val (lenSize, msgSize) = decodeMsgSize(start, msg)

        val content = msg.sliceArray(start+lenSize..msg.size)
        if (String(content).startsWith(head)){
            return msg
        }

        val len = encodeMsgSize("$head ".toByteArray().size + msgSize)
        return msg.copyOfRange(0, start) + len + "$head ".toByteArray() + msg.copyOfRange(start + lenSize, msg.size)
    }

    fun argsToString(args: Array<*>?): String? {
        val str = args?.joinToString(", ")
        return "{$str}"
    }

    fun bundleToString(bundle: Bundle?): String? {
        val str = bundle?.keySet()?.joinToString(", ") {
            "$it = ${bundle[it]}"
        }
        return "{$str}"
    }

    private val TWO_64 = BigInteger.ONE.shiftLeft(64)
    fun longToDecimalString(l: Long): String {
        var b = BigInteger.valueOf(l)
        if (b.signum() < 0) {
            b = b.add(TWO_64)
        }
        return b.toString()
    }

    fun bytesToHexString(arg: ByteArray?): String {
        if (arg == null) return ""
        return arg.joinToString("") { String.format("%02X", it) }
    }

    fun hexStringToBytes(arg: String?): ByteArray {
        if (arg == null || arg.isEmpty()) {
            return byteArrayOf()
        }
        val byte = Integer.parseInt(arg.slice(0..1), 16).toByte()
        return byteArrayOf(byte) + hexStringToBytes(arg.drop(2))
    }

    // encodeMsgSize encodes the size of a moment.
    fun encodeMsgSize(msgSize: Int): ByteArray {
        return if (msgSize shr 7 > 0)
            byteArrayOf (
                    (msgSize and 0x7F or 0x80).toByte(),
                    (msgSize shr 7).toByte()
            )
        else
            byteArrayOf(msgSize.toByte())
    }

    // decodeMsgSize decodes the length of a moment.
    fun decodeMsgSize(start: Int, msg: ByteArray): Pair<Int, Int> {
        var lenSize = 1
        var msgSize = msg[start].toInt()
        if (msgSize < 0) {
            lenSize = 2
            msgSize = (msgSize and 0x7F) + (msg[start + 1].toInt() shl 7)
        }
        return Pair(lenSize, msgSize)
    }
}