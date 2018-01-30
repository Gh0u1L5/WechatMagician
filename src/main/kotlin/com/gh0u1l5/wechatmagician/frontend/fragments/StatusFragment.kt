package com.gh0u1l5.wechatmagician.frontend.fragments

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_HOOK_STATUS
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_DATABASE
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_MSG_STORAGE
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_RESOURCES
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_URI_ROUTER
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_XML_PARSER
import com.gh0u1l5.wechatmagician.R
import kotlinx.android.synthetic.main.fragment_status.*

class StatusFragment : Fragment() {

    private val componentMap = mapOf(
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
            val context = context ?: return

            // Set the main banner of status fragment.
            val color: Int
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || getXposedVersion() >= 89) {
                color = ContextCompat.getColor(context, R.color.ok)
                status_text.text = getString(R.string.status_ok)
                status_image.setImageResource(R.drawable.ic_status_ok)
                status_image.contentDescription = getString(R.string.status_ok)
            } else {
                color = ContextCompat.getColor(context, R.color.warn)
                status_text.text = getString(R.string.status_outdated_xposed)
                status_image.setImageResource(R.drawable.ic_status_error)
                status_image.contentDescription = getString(R.string.status_outdated_xposed)
            }
            status_text.setTextColor(color)
            status_image.setBackgroundColor(color)

            // Set the status for each component.
            requireHookStatus(context, { status ->
                for (entry in componentMap) {
                    if (status[entry.key] == true) {
                        setComponentIconValid(entry.value)
                    }
                }
            })
        }
    }

    // Check backend.WechatHook for actual implementation
    private fun isModuleLoaded(): Boolean = false

    // Check backend.WechatHook for actual implementation
    private fun getXposedVersion(): Int = 0

    private fun setComponentIconValid(iconId: Int) {
        val icon = activity?.findViewById<ImageView>(iconId)
        if (icon != null) {
            icon.setImageResource(R.drawable.ic_component_valid)
            icon.contentDescription = getString(R.string.status_component_valid)
        }
    }

    companion object {
        fun newInstance(): StatusFragment = StatusFragment()

        fun requireHookStatus(context: Context, callback: (HashMap<String, Boolean>) -> Unit) {
            context.sendOrderedBroadcast(Intent(ACTION_REQUIRE_HOOK_STATUS), null, object : BroadcastReceiver() {
                @Suppress("UNCHECKED_CAST")
                override fun onReceive(context: Context?, intent: Intent?) {
                    val result = getResultExtras(true)
                    val status = result.getSerializable("status")
                    callback(status as HashMap<String, Boolean>? ?: return)
                }
            }, null, Activity.RESULT_OK, null, null)
        }
    }
}
