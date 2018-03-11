package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.contact

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxClasses
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.util.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassIfExists
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassesFromPackage

object Classes {
    private val classesInCurrentPackage by wxLazy("$wxPackageName.ui.contact") {
        findClassesFromPackage(wxLoader!!, wxClasses!!, "$wxPackageName.ui.contact")
    }

    val AddressUI: Class<*> by wxLazy("AddressUI") {
        findClassIfExists("$wxPackageName.ui.contact.AddressUI.a", wxLoader)
    }

    val AddressAdapter: Class<*> by wxLazy("AddressAdapter") {
        classesInCurrentPackage
                .filterByMethod(null, "pause")
                .firstOrNull()
    }

    val ContactLongClickListener: Class<*> by wxLazy("ContactLongClickListener") {
        classesInCurrentPackage
                .filterByEnclosingClass(AddressUI)
                .filterByMethod(C.Boolean, "onItemLongClick", C.AdapterView, C.View, C.Int, C.Long)
                .firstOrNull()
    }

    val SelectContactUI: Class<*> by wxLazy("SelectContactUI") {
        findClassIfExists("$wxPackageName.ui.contact.SelectContactUI", wxLoader)
    }
}