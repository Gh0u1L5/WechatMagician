package com.gh0u1l5.wechatmagician.spellbook.interfaces

import android.app.Activity

interface IActivityHook {

    /**
     * Called when a Wechat AlbumPreviewUI has been created. User can pick some pictures in it.
     *
     * @param activity The Activity object of the AlbumPreviewUI.
     */
    fun onAlbumPreviewUICreated(activity: Activity) { }

    /**
     * Called when a Wechat ChattingUI has been created. User can chat with another user or chatting
     * group in it.
     *
     * @param activity The Activity object of the ChattingUI.
     */
    fun onChattingUICreated(activity: Activity) { }

    /**
     * Called when a Wechat WebLoginUI has been created. User can approve a login request in it.
     *
     * @param activity The Activity object of the WebLoginUI.
     */
    fun onWebLoginUICreated(activity: Activity) { }

    /**
     * Called when a Wechat SnsTimelineUI has been created. User can explore the moments shared by
     * all the other friends in it.
     *
     * @param activity The Activity object of the SnsTimelineUI.
     */
    fun onSnsTimelineUICreated(activity: Activity) { }

    /**
     * Called when a Wechat SnsUserUI has been created. User can explore the moments posted by
     * himself / herself in it.
     *
     * @param activity The Activity object of the SnsUserUI.
     */
    fun onSnsUserUICreated(activity: Activity) { }

    /**
     * Called when a Wechat SnsUploadUI has been created. User can post a new moment to other
     * friends in it.
     *
     * @param activity The Activity object of the SnsUploadUI.
     */
    fun onSnsUploadUICreated(activity: Activity) { }
}