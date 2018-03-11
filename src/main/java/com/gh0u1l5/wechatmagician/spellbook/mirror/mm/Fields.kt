package com.gh0u1l5.wechatmagician.spellbook.mirror.mm

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.Classes.ImgInfoStorage
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.Classes.LruCacheWithListener
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findFieldsWithGenericType
import java.lang.reflect.Field

object Fields {
    val ImgInfoStorage_mBitmapCache: Field by wxLazy("ImgInfoStorage_mBitmapCache") {
        findFieldsWithGenericType(
                ImgInfoStorage, "${LruCacheWithListener.canonicalName}<java.lang.String, android.graphics.Bitmap>")
                .firstOrNull()?.apply { isAccessible = true }
    }
}