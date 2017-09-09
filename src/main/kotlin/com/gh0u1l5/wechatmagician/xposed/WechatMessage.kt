package com.gh0u1l5.wechatmagician.xposed

data class WechatMessage(val msgId: Long, val type: Int, val talker: String, val content: String) {
    val time: Long = System.currentTimeMillis()
}