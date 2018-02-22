package com.gh0u1l5.wechatmagician.backend.interfaces

import android.view.View
import android.widget.BaseAdapter

interface IAdapterHook {
    fun onAddressAdapterCreated(adapter: BaseAdapter) { }
    fun onConversationAdapterCreated(adapter: BaseAdapter) { }
    fun onSnsUserUIAdapterGetView(adapter: Any, convertView: View?, view: View) { }
}