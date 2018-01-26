package com.gh0u1l5.wechatmagician.frontend.fragments

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider.getUriForFile
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.ACTION_REQUIRE_WECHAT_PACKAGE
import com.gh0u1l5.wechatmagician.Global.LOG_TAG
import com.gh0u1l5.wechatmagician.Global.XPOSED_BASE_DIR
import com.gh0u1l5.wechatmagician.Global.XPOSED_FILE_PROVIDER
import com.gh0u1l5.wechatmagician.R
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
        support_email_card.setOnClickListener {
            generateReport()
        }
    }

    private fun getXposedLog(): Uri {
        val file = File("$XPOSED_BASE_DIR/log/error.log")
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Uri.fromFile(file)
        } else {
            getUriForFile(context!!, XPOSED_FILE_PROVIDER, file)
        }
    }

    private fun generateReport() {
        Toast.makeText(
                context, getString(R.string.prompt_wait), Toast.LENGTH_SHORT
        ).show()
        context?.sendOrderedBroadcast(Intent(ACTION_REQUIRE_WECHAT_PACKAGE), null, object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val greetings = getString(R.string.support_report_greetings)
                if (resultData == null) {
                    sendReport(greetings)
                } else {
                    sendReport("$resultData\n\n$greetings")
                }
            }
        }, null, RESULT_OK, null, null)
    }

    private fun sendReport(report: String) {
        try {
            view?.context?.startActivity(Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "text/plain"
//                putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(getXposedLog()))
                putExtra(Intent.EXTRA_EMAIL, arrayOf("WechatMagician@yahoo.com"))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_report_subject))
                putExtra(Intent.EXTRA_TEXT, report)
            })
        } catch (t: Throwable) {
            Log.e(LOG_TAG, "Cannot send email: $t")
            Toast.makeText(view?.context, t.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(): SupportFragment = SupportFragment()
    }
}
