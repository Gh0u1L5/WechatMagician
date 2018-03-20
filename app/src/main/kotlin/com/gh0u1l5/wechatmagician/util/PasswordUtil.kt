package com.gh0u1l5.wechatmagician.util

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.SALT
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.backend.storage.Strings
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px
import java.security.MessageDigest

object PasswordUtil {

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
        val okay: String
        val cancel: String

        if (context.applicationInfo.packageName == MAGICIAN_PACKAGE_NAME) {
            okay = context.getString(R.string.button_ok)
            cancel = context.getString(R.string.button_cancel)
        } else {
            okay = Strings.getString(R.string.button_ok)
            cancel = Strings.getString(R.string.button_cancel)
        }

        val input = EditText(context).apply {
            maxLines = 1
            hint = message
            inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
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
            addView(input)
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
        }

        val builder = AlertDialog.Builder(context)
                .setTitle(title)
                .setView(content)
        builder.setPositiveButton(okay) { dialog, _ ->
            onFinish(input.text.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton(cancel) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    fun askPasswordWithVerify(context: Context, title: String, message: String, encrypted: String, onSuccess: (String) -> Unit) {
        val promptCorrectPassword: String
        val promptWrongPassword: String

        if (context.applicationInfo.packageName == MAGICIAN_PACKAGE_NAME) {
            promptCorrectPassword = context.getString(R.string.prompt_correct_password)
            promptWrongPassword = context.getString(R.string.prompt_wrong_password)
        } else {
            promptCorrectPassword = Strings.getString(R.string.prompt_correct_password)
            promptWrongPassword = Strings.getString(R.string.prompt_wrong_password)
        }

        askPassword(context, title, message) { input ->
            val result = PasswordUtil.verifyPassword(encrypted, input)
            if (result) {
                onSuccess(input)
                Toast.makeText(context, promptCorrectPassword, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, promptWrongPassword, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun createPassword(context: Context, title: String, preferences: SharedPreferences, key: String, onFinish: (String) -> Unit = {}) {
        val message = if (context.applicationInfo.packageName == MAGICIAN_PACKAGE_NAME) {
            context.getString(R.string.prompt_setup_password)
        } else {
            Strings.getString(R.string.prompt_setup_password)
        }

        askPassword(context, title, message) { input ->
            preferences.edit()
                    .putString(key, encryptPassword(input))
                    .apply()
            onFinish(input)
        }
    }

    fun changePassword(context: Context, title: String, preferences: SharedPreferences, key: String, onFinish: (String) -> Unit = {}) {
        val message = if (context.applicationInfo.packageName == MAGICIAN_PACKAGE_NAME) {
            context.getString(R.string.prompt_verify_password)
        } else {
            Strings.getString(R.string.prompt_verify_password)
        }
        val encrypted = preferences.getString(key, "")
        askPasswordWithVerify(context, title, message, encrypted) {
            createPassword(context, title, preferences, key, onFinish)
        }
    }
}