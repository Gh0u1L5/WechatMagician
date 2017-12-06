package com.gh0u1l5.wechatmagician.util

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.SALT
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.BUTTON_CANCEL
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.BUTTON_OK
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_CORRECT_PASSWORD
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_NEW_PASSWORD
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_VERIFY_PASSWORD
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_WRONG_PASSWORD
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px
import java.security.MessageDigest

object PasswordUtil {

    private val str = LocalizedStrings

    private fun encryptPassword(password: String): String = MessageDigest
            .getInstance("SHA-256")
            .digest((password + SALT).toByteArray())
            .joinToString(separator = "") { String.format("%02X", it) }

    private fun verifyPassword(originSHA: String, password: String): Boolean {
        val inputSHA = MessageDigest
                .getInstance("SHA-256")
                .digest((password + SALT).toByteArray())
                .joinToString(separator = "") { String.format("%02X", it) }
        return inputSHA == originSHA
    }

    private fun askPassword(context: Context, title: String, message: String, onFinish: (String) -> Unit) {
        val input = EditText(context).apply {
            maxLines = 1
            inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = context.dp2px(25)
                marginEnd = context.dp2px(25)
            }
        }
        val content = LinearLayout(context).apply {
            addView(input)
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
        }

        val builder = AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(content)
        builder.setPositiveButton(str[BUTTON_OK]) { dialog, _ ->
            onFinish(input.text.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton(str[BUTTON_CANCEL]) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    fun askPasswordWithVerify(context: Context, title: String, message: String, encrypted: String, onSuccess: (String) -> Unit) {
        askPassword(context, title, message) { input ->
            val result = PasswordUtil.verifyPassword(encrypted, input)
            if (result) {
                onSuccess(input)
                Toast.makeText(
                        context, str[PROMPT_CORRECT_PASSWORD], Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                        context, str[PROMPT_WRONG_PASSWORD], Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun createPassword(context: Context, title: String, preferences: SharedPreferences, key: String) {
        val message = str[PROMPT_NEW_PASSWORD]
        askPassword(context, title, message) { input ->
            preferences.edit()
                    .putString(key, encryptPassword(input))
                    .apply()
        }
    }

    fun changePassword(context: Context, title: String, preferences: SharedPreferences, key: String) {
        val message = str[PROMPT_VERIFY_PASSWORD]
        val encrypted = preferences.getString(key, "")
        askPasswordWithVerify(context, title, message, encrypted) {
            createPassword(context, title, preferences, key)
        }
    }
}