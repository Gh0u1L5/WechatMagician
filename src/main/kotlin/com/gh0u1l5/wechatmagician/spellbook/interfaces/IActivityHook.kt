package com.gh0u1l5.wechatmagician.spellbook.interfaces

import android.app.Activity

interface IActivityHook {

    fun onAlbumPreviewUICreated(activity: Activity) { }

    fun onChattingUICreated(activity: Activity) { }

    fun onWebLoginUICreated(activity: Activity) { }

    fun onSnsTimelineUICreated(activity: Activity) { }

    fun onSnsUploadUICreated(activity: Activity) { }

    fun onSnsUserUICreated(activity: Activity) { }
}