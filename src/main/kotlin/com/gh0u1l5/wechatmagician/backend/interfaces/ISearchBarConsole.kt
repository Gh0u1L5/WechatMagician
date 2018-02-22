package com.gh0u1l5.wechatmagician.backend.interfaces

import android.app.AlertDialog
import android.content.Context

interface ISearchBarConsole {
    fun onHandleCommand(context: Context, command: String): Boolean {
        if (command.startsWith("alert ")) {
            val prompt = command.drop("alert ".length)
            AlertDialog.Builder(context)
                    .setTitle("Wechat Magician")
                    .setMessage(prompt)
                    .show()
            return true
        }
        return false
    }
}