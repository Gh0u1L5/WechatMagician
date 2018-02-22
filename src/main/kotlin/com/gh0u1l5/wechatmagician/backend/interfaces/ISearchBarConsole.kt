package com.gh0u1l5.wechatmagician.backend.interfaces

import android.content.Context

interface ISearchBarConsole {
    fun onHandleCommand(context: Context, command: String) = false
}