package com.gh0u1l5.wechatmagician.spellbook.hookers

import android.content.Context.INPUT_METHOD_SERVICE
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.gh0u1l5.wechatmagician.spellbook.Global.STATUS_FLAG_COMMAND
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.ActionBarEditText
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.hookers.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.ISearchBarConsole
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors

object SearchBar : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(ISearchBarConsole::class.java)

    private fun cleanup(search: EditText, editable: Editable?) {
        // Hide Input Method
        val imm = search.context.getSystemService(INPUT_METHOD_SERVICE)
        (imm as InputMethodManager).hideSoftInputFromWindow(search.windowToken, 0)
        // Clean SearchBar content
        editable?.clear()
    }

    @WechatHookMethod @JvmStatic fun hookEvents() {
        hookAllConstructors(ActionBarEditText, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val search = param.thisObject as EditText
                search.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                    override fun afterTextChanged(editable: Editable?) {
                        val command = editable.toString()
                        if (!command.endsWith("#")) {
                            return
                        }
                        notify("onHandleCommand") { plugin ->
                            if (plugin is ISearchBarConsole) {
                                val consumed = plugin.onHandleCommand(search.context, command.drop(1).dropLast(1))
                                if (consumed) {
                                    cleanup(search, editable)
                                }
                            }
                        }
                    }
                })
            }
        })

        WechatStatus.toggle(STATUS_FLAG_COMMAND, true)
    }
}
