package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import com.gh0u1l5.wechatmagician.spellbook.interfaces.ISearchBarConsole
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px

object Alert : ISearchBarConsole {

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    // Add "alert" command for SearchBar console.
    override fun onHandleCommand(context: Context, command: String): Boolean {
        if (command.startsWith("alert ")) {
            val prompt = TextView(context).apply {
                setTextColor(Color.BLACK)
                text = command.drop("alert ".length)
                textSize = 16.0F
                layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = context.dp2px(10)
                    bottomMargin = context.dp2px(10)
                    leftMargin = context.dp2px(25)
                    rightMargin = context.dp2px(25)
                }
            }
            val content = LinearLayout(context).apply {
                addView(prompt)
                layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
            }
            mainHandler.post {
                AlertDialog.Builder(context)
                        .setTitle("Wechat Magician")
                        .setView(content)
                        .show()
            }
            return true
        }
        return super.onHandleCommand(context, command)
    }
}