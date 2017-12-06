package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_COMMAND
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.plugins.SecretFriend.changeUserStatus
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_SET_PASSWORD
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_VERIFY_PASSWORD
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.TITLE_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.util.PasswordUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors

object SearchBar {

    private var preferences: Preferences? = null

    @JvmStatic fun init(_preferences: Preferences) {
        preferences = _preferences
    }

    private val str = LocalizedStrings
    private val pkg = WechatPackage

    private fun handleCommand(context: Context, command: String): Boolean {
        when {
            command.startsWith("#alert ") -> {
                val prompt = command.drop("#alert ".length)
                AlertDialog.Builder(context)
                        .setTitle("Wechat Magician")
                        .setMessage(prompt)
                        .show()
                return true
            }
            command.startsWith("#hide ") -> {
                if (!preferences!!.getBoolean("settings_secret_friend", false)) {
                    return false
                }

                val encrypted = preferences!!.getString("settings_secret_friend_password", "")
                if (encrypted == "") {
                    Toast.makeText(
                            context, str[PROMPT_SET_PASSWORD], Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val nickname = command.drop("#hide ".length)
                    changeUserStatus(context, nickname, true)
                }
                return true
            }
            command.startsWith("#unhide ") -> {
                if (!preferences!!.getBoolean("settings_secret_friend", false)) {
                    return false
                }

                val encrypted = preferences!!.getString("settings_secret_friend_password", "")
                if (encrypted == "") {
                    Toast.makeText(
                            context, str[PROMPT_SET_PASSWORD], Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val title = str[TITLE_SECRET_FRIEND]
                    val message = str[PROMPT_VERIFY_PASSWORD]
                    PasswordUtil.askPasswordWithVerify(context, title, message, encrypted) {
                        val nickname = command.drop("#unhide ".length)
                        SecretFriend.changeUserStatus(context, nickname, false)
                    }
                }
                return true
            }
            else -> return false
        }
    }

    @JvmStatic fun hijackSearchBar() {
        if (pkg.ActionBarEditText == null) {
            return
        }

        hookAllConstructors(pkg.ActionBarEditText, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val search = param.thisObject as EditText
                search.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                    override fun afterTextChanged(editable: Editable?) {
                        val command = editable.toString()
                        if (command.endsWith("#")) {
                            val consumed = handleCommand(search.context, command.dropLast(1))
                            if (consumed) editable?.clear()
                        }
                    }
                })
            }
        })

        pkg.setStatus(STATUS_FLAG_COMMAND, true)
    }
}
