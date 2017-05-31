package com.rarnu.norevoke.util

object MessageUtil {

    fun customize(str: String?): String? {
        return str?.let{ "${it.substring(1, it.indexOf("\"", 1))} 妄图撤回一条消息，啧啧" }
    }

    fun notifyChatroomRecall(head: String, msg: String): String {
        val index = msg.indexOf(":\n")
        if (msg.substring(index + 2).startsWith(head)) return msg
        return "${msg.substring(0, index + 2)}$head ${msg.substring(index + 2)}"
    }

    fun notifyPrivateRecall(head: String, msg: String): String {
        if (msg.startsWith(head)) return msg
        return "$head $msg"
    }

    fun notifyInfoDelete(head: String?, msg: ByteArray?): ByteArray? {
        if (head == null || msg == null) return null

        val start = msg.indexOf(0x2A) + 1
        val content = decodeSnsMsg(msg.sliceArray(start..msg.size))
        if (String(content.first).startsWith(head)) return msg

        val ntext = encodeSnsMsg("$head ".toByteArray() + content.first)
        return msg.copyOfRange(0, start) + ntext + msg.copyOfRange(start + content.second, msg.size)
    }

    fun notifyCommentDelete(head: String?, msg: ByteArray?): ByteArray? {
        if (head == null || msg == null) return null

        val namestart = msg.indexOf(0x22)
        val start = namestart + byteToUnsignedInt(msg[namestart + 1]) + 13
        val content = decodeSnsMsg(msg.sliceArray(start..msg.size))
        if (String(content.first).startsWith(head)) return msg

        val ntext = encodeSnsMsg("$head ".toByteArray() + content.first)
        return msg.copyOfRange(0, start) + ntext + msg.copyOfRange(start + content.second, msg.size)
    }

    fun argsToString(arg: Array<*>?): String {
        if (arg == null) return ""
        return arg.joinToString(", ")
    }

    fun bytesToHexString(arg: ByteArray?): String {
        if (arg == null) return ""
        return arg.map{ String.format("%02X", it) }.joinToString("")
    }
    
    fun hexStringToBytes(arg: String?): ByteArray {
        if (arg == null || arg.isEmpty()) return byteArrayOf()
        val byte = Integer.parseInt(arg.slice(0..1), 16).toByte()
        return byteArrayOf(byte) + hexStringToBytes(arg.drop(2))
    }

    fun byteToUnsignedInt(arg: Byte): Int {
        return arg.toInt() and 0xFF
    }

    fun encodeSnsMsg(msg: ByteArray): ByteArray {
        if (msg.size >= 0x80)
            return byteArrayOf((msg.size and 0xFF).toByte(), ((msg.size shr 7) + 1).toByte()) + msg
        else
            return byteArrayOf(msg.size.toByte()) + msg
    }

    fun decodeSnsMsg(msg: ByteArray): Pair<ByteArray, Int> {
        var llen = 1
        var mlen = byteToUnsignedInt(msg[0])
        if (byteToUnsignedInt(msg[1]) <= 0x1F) {
            llen += 1
            mlen += (byteToUnsignedInt(msg[1]) - 1) shl 7
        }
        return Pair(msg.copyOfRange(llen, mlen + llen), mlen + llen)
    }
}