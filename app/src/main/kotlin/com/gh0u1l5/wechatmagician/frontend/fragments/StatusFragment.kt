package com.gh0u1l5.wechatmagician.frontend.fragments

import android.app.Activity
import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_HOOK_STATUS
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus.StatusFlag.*
import kotlinx.android.synthetic.main.fragment_status.*

class StatusFragment : Fragment() {

    // TODO: add local cache for status

    private val componentMap = mapOf (
            STATUS_FLAG_MSG_STORAGE to R.id.component_msg_storage_status,
            STATUS_FLAG_RESOURCES to R.id.component_resources_status,
            STATUS_FLAG_DATABASE to R.id.component_database_status,
            STATUS_FLAG_XML_PARSER to R.id.component_xml_parser_status,
            STATUS_FLAG_URI_ROUTER to R.id.component_uri_router_status
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_status, container, false)

    override fun onStart() {
        super.onStart()

        if (isModuleLoaded()) {
            // Set the main banner of status fragment.
            val color: Int
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || getXposedVersion() >= 89) {
                color = ContextCompat.getColor(activity!!, R.color.ok)
                status_text.text = getString(R.string.status_ok)
                status_image.setImageResource(R.drawable.ic_status_ok)
                status_image.contentDescription = getString(R.string.status_ok)
            } else {
                color = ContextCompat.getColor(activity!!, R.color.warn)
                status_text.text = getString(R.string.status_outdated_xposed)
                status_image.setImageResource(R.drawable.ic_status_error)
                status_image.contentDescription = getString(R.string.status_outdated_xposed)
            }
            status_text.setTextColor(color)
            status_image.setBackgroundColor(color)

            // Set the status for each component.
            requireHookStatus(activity!!, { status ->
                for (entry in componentMap) {
                    if (status.contains(entry.key)) {
                        setComponentIconValid(entry.value)
                    }
                }
            })
        }
    }

    // Check backend.WechatHook for actual implementation
    private fun isModuleLoaded(): Boolean {
        // In some frameworks, short methods (less than two Dalvik instructions)
        // can not be hooked stably. This log just makes the method longer to hook.
        Log.v(TAG, "$javaClass.isModuleLoaded() invoked.")
        return false
    }

    // Check backend.WechatHook for actual implementation
    private fun getXposedVersion(): Int {
        // In some frameworks, short methods (less than two Dalvik instructions)
        // can not be hooked stably. This log just makes the method longer to hook.
        Log.v(TAG, "$javaClass.getXposedVersion() invoked. ")
        return 0
    }

    private fun setComponentIconValid(iconId: Int) {
        val icon = activity?.findViewById<ImageView>(iconId)
        if (icon != null) {
            icon.setImageResource(R.drawable.ic_component_valid)
            icon.contentDescription = getString(R.string.status_component_valid)
        }
    }

    companion object {
        private const val TAG = "StatusFragment"

        fun newInstance(): StatusFragment = StatusFragment()

        fun requireHookStatus(context: Context, callback: (List<WechatStatus.StatusFlag>) -> Unit) {
            val intent = Intent(ACTION_REQUIRE_HOOK_STATUS).addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            context.sendOrderedBroadcast(intent, null, object : BroadcastReceiver() {
                @Suppress("UNCHECKED_CAST")
                override fun onReceive(context: Context?, intent: Intent?) {
                    val result = getResultExtras(true)
                    val status = result.getIntArray("status")
                    if (status != null) {
                        val flags = WechatStatus.StatusFlag.values()
                        callback(status.map { flags[it] })
                    }
                }
            }, null, Activity.RESULT_OK, null, null)
        }
    }
}
