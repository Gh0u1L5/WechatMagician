package com.gh0u1l5.wechatmagician.backend.storage.cache

// SnsCache records the timeline objects browsed by the user.
object SnsCache : BaseCache<String, SnsCache.SnsInfo>() {

    data class SnsMediaURL(
            val url: String?,
            val md5: String?,
            val idx: String?,
            val key: String?,
            val token: String?
    )

    data class SnsMedia(
            val type: String?,
            val main: SnsMediaURL?,
            val thumb: SnsMediaURL?
    )

    class SnsInfo(raw: MutableMap<String, String>) {
        companion object {
            private const val MEDIA_LIST_KEY = ".TimelineObject.ContentObject.mediaList"
            private const val WECHAT_SUPPORT_DOMAIN = "https://support.weixin.qq.com"
        }

        val title = raw[".TimelineObject.ContentObject.title"]
        val content = raw[".TimelineObject.contentDesc"]
        val contentUrl: String?
        val medias = parseMedias(raw)

        init {
            val url = raw[".TimelineObject.ContentObject.contentUrl"]
            this.contentUrl = when {
                url == null || url.isEmpty() -> null
                url.startsWith(WECHAT_SUPPORT_DOMAIN) -> null
                else -> url
            }
        }

        private fun parseMediaURL(key: String, raw: MutableMap<String, String>): SnsMediaURL? {
            if (!raw.containsKey("$MEDIA_LIST_KEY.$key")) {
                return null
            }
            return SnsMediaURL(
                    url = raw["$MEDIA_LIST_KEY.$key"],
                    md5 = raw["$MEDIA_LIST_KEY.$key.\$md5"],
                    idx = raw["$MEDIA_LIST_KEY.$key.\$enc_idx"],
                    key = raw["$MEDIA_LIST_KEY.$key.\$key"],
                    token = raw["$MEDIA_LIST_KEY.$key.\$token"]
            )
        }

        private fun parseMedia(key: String, raw: MutableMap<String, String>): SnsMedia? {
            if (key == "media0") {
                return parseMedia("media", raw)
            }
            if (!raw.containsKey("$MEDIA_LIST_KEY.$key")) {
                return null
            }
            return SnsMedia(
                    type = raw["$MEDIA_LIST_KEY.$key.type"],
                    main = parseMediaURL("$key.url", raw),
                    thumb = parseMediaURL("$key.thumb", raw)
            )
        }

        private fun parseMedias(raw: MutableMap<String, String>): List<SnsMedia> {
            if (!raw.containsKey(MEDIA_LIST_KEY)) {
                return emptyList()
            }
            val result = mutableListOf<SnsMedia>()
            for (i in 0 until 9) {
                result += parseMedia("media$i", raw) ?: break
            }
            return result.toList()
        }
    }
}