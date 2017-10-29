package com.gh0u1l5.wechatmagician.frontend.fragments

import android.app.Fragment
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.storage.HookStatus
import com.gh0u1l5.wechatmagician.util.FileUtil
import com.gh0u1l5.wechatmagician.util.ViewUtil.getColor
import kotlinx.android.synthetic.main.fragment_status.*

class StatusFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_status, container, false)
    }

    override fun onStart() {
        super.onStart()

        val colorOk = getColor(activity, resources, R.color.ok)
        if (isModuleLoaded()) {
            status_text.setTextColor(colorOk)
            status_text.text = getString(R.string.status_ok)
            status_image.setBackgroundColor(colorOk)
            status_image.setImageResource(R.drawable.ic_status_ok)
            status_image.contentDescription = getString(R.string.status_ok)
            val status = readComponentStatus()
            showComponentStatus(status)
        }
    }

    private fun isModuleLoaded(): Boolean {
        // Check backend.plugins.Frontend for actual implementation
        return false
    }

    private fun readComponentStatus(): HookStatus {
        val storage = Environment.getExternalStorageDirectory().absolutePath + "/WechatMagician"
        try {
            val status = FileUtil.readObjectFromDisk("$storage/.status/hooks")
            if (status is HookStatus) {
                return status
            }
        } catch (e: Throwable) {
            Toast.makeText(
                    activity, R.string.prompt_load_component_status_failed, Toast.LENGTH_SHORT
            ).show()
        }
        return HookStatus()
    }

    private fun showComponentStatus(status: HookStatus) {
        if (status.MsgStorage) {
            setComponentIconValid(component_msg_storage_status)
        }
        if (status.Resources) {
            setComponentIconValid(component_resources_status)
        }
        if (status.Database) {
            setComponentIconValid(component_database_status)
        }
        if (status.XMLParser) {
            setComponentIconValid(component_xml_parser_status)
        }
        if (status.CustomScheme) {
            setComponentIconValid(component_custom_scheme_status)
        }
    }

    private fun setComponentIconValid(icon: ImageView) {
        icon.setImageResource(R.drawable.ic_component_valid)
        icon.contentDescription = getString(R.string.status_component_valid)
    }

    companion object {
        fun newInstance(): StatusFragment {
            return StatusFragment()
        }
    }
}
