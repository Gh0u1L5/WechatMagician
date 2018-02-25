package com.gh0u1l5.wechatmagician.spellbook.interfaces

import android.view.View
import android.widget.BaseAdapter

interface IAdapterHook {

    fun onAddressAdapterCreated(adapter: BaseAdapter) { }

    fun onConversationAdapterCreated(adapter: BaseAdapter) { }

    fun onSnsUserUIAdapterGotView(adapter: Any, convertView: View?, view: View) { }
}