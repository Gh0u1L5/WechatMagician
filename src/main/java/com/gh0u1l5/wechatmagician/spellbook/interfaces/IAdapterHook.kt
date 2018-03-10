package com.gh0u1l5.wechatmagician.spellbook.interfaces

import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.BaseAdapter

interface IAdapterHook {

    /**
     * Called when a Wechat AddressAdapter has been created. This adapter will be used in the
     * ListView for all the contacts (which is shown in the second tab of Wechat).
     *
     * @param adapter The created AddressAdapter object.
     */
    fun onAddressAdapterCreated(adapter: BaseAdapter) { }

    /**
     * Called when a Wechat ConversationAdapter has been created. This adapter will be used in the
     * ListView for all the conversations (which is shown in the first tab of Wechat).
     *
     * @param adapter The created ConversationAdapter object.
     */
    fun onConversationAdapterCreated(adapter: BaseAdapter) { }

    /**
     * Called when the [Adapter.getView] function for a Wechat SnsUserUIAdapter has finished. This
     * adapter is used in the ListView for all the moments posted by user himself / herself.
     *
     * @param adapter The SnsUserUIAdapter object which handle the getView().
     * @param position The position of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to
     * @param result The view that is returned by the original getView() function.
     */
    fun onSnsUserUIAdapterGotView(adapter: Any, position: Int, convertView: View?, parent: ViewGroup, result: View) { }
}