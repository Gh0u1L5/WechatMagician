package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.AlertDialog
import android.content.Context
import com.gh0u1l5.wechatmagician.spellbook.interfaces.ISearchBarConsole

object Alert : ISearchBarConsole {
    // Add "alert" command for SearchBar console.
    override fun onHandleCommand(context: Context, command: String): Boolean {
        if (command.startsWith("alert ")) {
            val prompt = command.drop("alert ".length)
            AlertDialog.Builder(context)
                    .setTitle("Wechat Magician")
                    .setMessage(prompt)
                    .show()
            return true
        }
        return super.onHandleCommand(context, command)
    }
}