package com.gh0u1l5.wechatmagician.frontend.fragments

import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gh0u1l5.wechatmagician.R
import kotlinx.android.synthetic.main.fragment_support.*

class SupportFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_support, container, false)
    }

    override fun onStart() {
        super.onStart()
        support_github_card.setOnClickListener { view ->
            val url = Uri.parse("${view?.context?.getString(R.string.view_about_project_github_url)}/issues")
            view?.context?.startActivity(Intent(Intent.ACTION_VIEW).setData(url))
        }
        support_email_card.setOnClickListener { view ->
            view?.context?.startActivity(Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("WechatMagician@yahoo.com"))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_report_subject))
            })
        }
    }

    companion object {
        fun newInstance(): SupportFragment {
            return SupportFragment()
        }
    }
}
