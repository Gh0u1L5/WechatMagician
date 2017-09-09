package com.gh0u1l5.wechatmagician.xposed

class WechatMessage(val msgId: Int, val type: Int, val talker: String, var content: String) {
    val time: Long = System.currentTimeMillis()
    init { if (type != 1) content = "" }
}