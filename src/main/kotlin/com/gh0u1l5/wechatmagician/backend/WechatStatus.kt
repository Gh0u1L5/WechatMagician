package com.gh0u1l5.wechatmagician.backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_STATUS
import com.gh0u1l5.wechatmagician.Global.INTENT_STATUS_FIELDS
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object WechatStatus {

    private val statusLock = ReentrantReadWriteLock()
    private val status: MutableMap<String, Boolean> = mutableMapOf()

    operator fun set(flag: String, value: Boolean) {
        statusLock.write {
            status[flag] = value
        }
    }

    operator fun get(flag: String): Boolean? {
        statusLock.read {
            return status[flag]
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val fields = intent?.getStringArrayExtra(INTENT_STATUS_FIELDS)
            val extras = Bundle()
            statusLock.read {
                fields?.forEach { field ->
                    val value = status[field]
                    if (value != null) {
                        extras.putBoolean(field, value)
                    }
                }
            }
            setResultExtras(extras)
            abortBroadcast()
        }
    }

    fun listen(context: Context?) {
        context?.registerReceiver(receiver, IntentFilter(ACTION_REQUIRE_STATUS))
    }
}