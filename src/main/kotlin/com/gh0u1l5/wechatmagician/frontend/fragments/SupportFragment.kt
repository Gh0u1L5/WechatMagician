package com.gh0u1l5.wechatmagician.frontend.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.LOG_TAG
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.Global.XPOSED_PACKAGE_NAME
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.util.FileUtil.getApplicationDataDir
import com.gh0u1l5.wechatmagician.util.ViewUtil.openURL
import kotlinx.android.synthetic.main.fragment_support.*
import java.io.File

class SupportFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_support, container, false)

    override fun onStart() {
        super.onStart()
        support_github_card.setOnClickListener { view ->
            openURL(activity, "${view?.context?.getString(R.string.view_about_project_github_url)}/issues")
        }
        support_email_card.setOnClickListener { view ->
            val dataDir = getApplicationDataDir(activity).replace(MAGICIAN_PACKAGE_NAME, XPOSED_PACKAGE_NAME)
            val xposedLog = Uri.fromFile(File("$dataDir/log/error.log"))
            try {
                view?.context?.startActivity(Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "text/plain"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(xposedLog))
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
        fun newInstance(): SupportFragment = SupportFragment()
    }
}
