package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.sns.ui

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.sns.ui.Classes.SnsUploadUI
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findFieldsWithType
import java.lang.reflect.Field

object Fields {
    val SnsUploadUI_mSnsEditText: Field by wxLazy("SnsUploadUI_mSnsEditText") {
        findFieldsWithType(SnsUploadUI, "$wxPackageName.plugin.sns.ui.SnsEditText")
                .firstOrNull()?.apply { isAccessible = true }
    }
}