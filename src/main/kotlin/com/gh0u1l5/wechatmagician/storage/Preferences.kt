package com.gh0u1l5.wechatmagician.storage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.gh0u1l5.wechatmagician.Global.ACTION_DESIRE_PREF
import com.gh0u1l5.wechatmagician.Global.ACTION_UPDATE_PREF
import com.gh0u1l5.wechatmagician.Global.INTENT_PREF_KEYS
import com.gh0u1l5.wechatmagician.Global.INTENT_PREF_NAME
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class Preferences {

    // content stores a map that sync to the settings from frontend.
    private val contentLock = ReentrantReadWriteLock()
    private val content= mutableMapOf<String, Any?>()

    // receiver listens the frontend and updates content as needed.
    private val receiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val keys = intent?.getStringArrayExtra(INTENT_PREF_KEYS)
            keys?.forEach { key ->
                contentLock.write {
                    content[key] = intent.extras[key]
                }
            }
        }
    }

    // load registers the receiver and requires the latest settings from frontend.
    fun load(context: Context?, preferencesName: String) {
        context?.registerReceiver(receiver, IntentFilter(ACTION_UPDATE_PREF))
        context?.sendBroadcast(Intent(ACTION_DESIRE_PREF).apply {
            putExtra(INTENT_PREF_KEYS, arrayOf("*"))
            putExtra(INTENT_PREF_NAME, preferencesName)
        })
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        contentLock.read {
            return content[key] as Boolean? ?: defValue
        }
    }

    fun getString(key: String, defValue: String = ""): String {
        contentLock.read {
            return content[key] as String? ?: defValue
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringList(key: String, defValue: Array<String> = arrayOf()): Array<String> {
        contentLock.read {
            return content[key] as Array<String>? ?: defValue
        }
    }
}