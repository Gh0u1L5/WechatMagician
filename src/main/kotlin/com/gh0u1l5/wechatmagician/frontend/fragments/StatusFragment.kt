package com.gh0u1l5.wechatmagician.frontend.fragments

import android.app.Activity.RESULT_OK
import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_STATUS
import com.gh0u1l5.wechatmagician.Global.INTENT_STATUS_FIELDS
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_CUSTOM_SCHEME
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_DATABASE
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_HOOKING
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_MSG_STORAGE
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_RESOURCES
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_XML_PARSER
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.util.ViewUtil.getColor
import kotlinx.android.synthetic.main.fragment_status.*

class StatusFragment : Fragment() {

    val componentMap = mapOf(
            STATUS_FLAG_MSG_STORAGE to R.id.component_msg_storage_status,
            STATUS_FLAG_RESOURCES to R.id.component_resources_status,
            STATUS_FLAG_DATABASE to R.id.component_database_status,
            STATUS_FLAG_XML_PARSER to R.id.component_xml_parser_status,
            STATUS_FLAG_CUSTOM_SCHEME to R.id.component_custom_scheme_status
    )

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_status, container, false)
    }

    override fun onStart() {
        super.onStart()

        val intent = Intent(ACTION_REQUIRE_STATUS).apply {
            val keys = componentMap.keys + STATUS_FLAG_HOOKING
            putExtra(INTENT_STATUS_FIELDS, keys.toTypedArray())
        }
        activity.sendOrderedBroadcast(intent, null, object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                status_progress_bar.visibility = GONE
                status_banner.visibility = VISIBLE
                status_components.visibility = VISIBLE

                val extras = getResultExtras(true)
                if (extras[STATUS_FLAG_HOOKING] == true) {
                    setBannerValid()
                    for (entry in componentMap) {
                        if (extras[entry.key] == true) {
                            setComponentIconValid(entry.value)
                        }
                    }
                }
            }
        }, null, RESULT_OK, null, null)
    }

    private fun setBannerValid() {
        val colorOk = getColor(activity, resources, R.color.ok)

        status_text.setTextColor(colorOk)
        status_text.text = getString(R.string.status_ok)
        status_image.setBackgroundColor(colorOk)
        status_image.setImageResource(R.drawable.ic_status_ok)
        status_image.contentDescription = getString(R.string.status_ok)
    }

    private fun setComponentIconValid(iconId: Int) {
        val icon = activity.findViewById<ImageView>(iconId)
        if (icon != null) {
            icon.setImageResource(R.drawable.ic_component_valid)
            icon.contentDescription = getString(R.string.status_component_valid)
        }
    }

    companion object {
        fun newInstance(): StatusFragment {
            return StatusFragment()
        }
    }
}
