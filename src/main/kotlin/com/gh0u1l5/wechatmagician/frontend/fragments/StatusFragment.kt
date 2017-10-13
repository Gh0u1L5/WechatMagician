package com.gh0u1l5.wechatmagician.frontend.fragments

import android.app.Fragment
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.gh0u1l5.wechatmagician.R

class StatusFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val layout = inflater!!.inflate(R.layout.fragment_status, container, false)
        val image = layout.findViewById<ImageView>(R.id.status_image)
        val text = layout.findViewById<TextView>(R.id.status_text)

        val colorOk = getColor(R.color.ok)
        val colorError = getColor(R.color.error)

        if (isModuleLoaded()) {
            text.setTextColor(colorOk)
            text.text = getString(R.string.status_ok)
            image.setBackgroundColor(colorOk)
            image.setImageResource(R.drawable.ic_status_ok)
            image.contentDescription = getString(R.string.status_ok)
        } else {
            text.setTextColor(colorError)
            text.text = getString(R.string.status_error)
            image.setBackgroundColor(colorError)
            image.setImageResource(R.drawable.ic_status_error)
            image.contentDescription = getString(R.string.status_error)
        }
        return layout
    }

    private fun isModuleLoaded(): Boolean {
        // Check backend.plugins.Frontend for actual implementation
        return false
    }

    private fun getColor(resId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(resId, context.theme)
        } else {
            resources.getColor(resId)
        }
    }

    companion object {
        fun newInstance(): StatusFragment {
            return StatusFragment()
        }
    }
}
