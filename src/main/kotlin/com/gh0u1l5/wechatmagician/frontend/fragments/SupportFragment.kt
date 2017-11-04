package com.gh0u1l5.wechatmagician.frontend.fragments

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.LOG_TAG
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.util.ViewUtil.openURL
import kotlinx.android.synthetic.main.fragment_support.*
import java.io.File

class SupportFragment : Fragment() {

    private val XPOSED_PKG = "de.robv.android.xposed.installer"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_support, container, false)
    }

    @SuppressLint("SdCardPath")
    override fun onStart() {
        super.onStart()
        support_github_card.setOnClickListener { view ->
            openURL(activity, "${view?.context?.getString(R.string.view_about_project_github_url)}/issues")
        }
        support_email_card.setOnClickListener { view ->
            val storage = Environment.getExternalStorageDirectory().absolutePath + "/WechatMagician"
            val pkgLog = Uri.fromFile(File("$storage/.status/pkg"))
            val xposedLog = Uri.fromFile(File("/data/data/$XPOSED_PKG/log/error.log"))
            try {
                view?.context?.startActivity(Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "text/plain"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(pkgLog, xposedLog))
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("WechatMagician@yahoo.com"))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_report_subject))
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.support_report_text))
                })
            } catch (e: Throwable) {
                Log.e(LOG_TAG, "Cannot send email: $e")
                Toast.makeText(view?.context, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(): SupportFragment {
            return SupportFragment()
        }
    }
}
