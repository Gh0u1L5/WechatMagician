package com.gh0u1l5.wechatmagician.frontend.fragments

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.gh0u1l5.wechatmagician.Global.FOLDER_SHARED
import com.gh0u1l5.wechatmagician.Global.LOG_TAG
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_CUSTOM_SCHEME
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_DATABASE
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_MSG_STORAGE
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_RESOURCES
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_XML_PARSER
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.util.FileUtil.getApplicationDataDir
import com.gh0u1l5.wechatmagician.util.FileUtil.readObjectFromDisk
import kotlinx.android.synthetic.main.fragment_status.*
import java.io.File

class StatusFragment : Fragment() {

    private val componentMap = mapOf(
            STATUS_FLAG_MSG_STORAGE to R.id.component_msg_storage_status,
            STATUS_FLAG_RESOURCES to R.id.component_resources_status,
            STATUS_FLAG_DATABASE to R.id.component_database_status,
            STATUS_FLAG_XML_PARSER to R.id.component_xml_parser_status,
            STATUS_FLAG_CUSTOM_SCHEME to R.id.component_custom_scheme_status
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_status, container, false)

    override fun onStart() {
        super.onStart()

        if (!isModuleLoaded()) {
            return
        }

        val context = context ?: return

        // Set the main banner of status fragment.
        val colorOk = ContextCompat.getColor(context, R.color.ok)
        status_text.setTextColor(colorOk)
        status_text.text = getString(R.string.status_ok)
        status_image.setBackgroundColor(colorOk)
        status_image.setImageResource(R.drawable.ic_status_ok)
        status_image.contentDescription = getString(R.string.status_ok)

        // Set the status for each component.
        val status = readHookStatus(context)
        if (status != null) {
            for (entry in componentMap) {
                if (status[entry.key] == true) {
                    setComponentIconValid(entry.value)
                }
            }
        }
    }

    // Check backend.plugins.Frontend for actual implementation
    private fun isModuleLoaded(): Boolean = false

    private fun setComponentIconValid(iconId: Int) {
        val icon = activity?.findViewById<ImageView>(iconId)
        if (icon != null) {
            icon.setImageResource(R.drawable.ic_component_valid)
            icon.contentDescription = getString(R.string.status_component_valid)
        }
    }

    companion object {
        fun newInstance(): StatusFragment = StatusFragment()

        @Suppress("UNCHECKED_CAST")
        fun readHookStatus(context: Context?): HashMap<String, Boolean>? {
            val dataDir = getApplicationDataDir(context)
            val sharedDir = File(dataDir, FOLDER_SHARED)
            if (sharedDir.exists()) {
                val path = sharedDir.absolutePath + "/status"

                // Check the modified time of the status object
                val bootAt = System.currentTimeMillis() - SystemClock.elapsedRealtime()
                val modifiedAt = File(path).lastModified()
                if (modifiedAt < bootAt) {
                    // status is not modified after this boot, invalid.
                    return null
                }

                // Read status object from disk
                try {
                    return readObjectFromDisk(path) as HashMap<String, Boolean>
                } catch (e: Throwable) {
                    Log.e(LOG_TAG, "Cannot read hooking status: $e")
                }
            }
            return null
        }
    }
}
