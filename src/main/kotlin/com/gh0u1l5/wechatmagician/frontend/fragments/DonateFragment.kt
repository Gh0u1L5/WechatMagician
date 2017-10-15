package com.gh0u1l5.wechatmagician.frontend.fragments

import android.app.Fragment
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.util.AlipayUtil
import kotlinx.android.synthetic.main.fragment_donate.*


class DonateFragment : Fragment() {

    private val alipayCode = "FKX04114Q6YBQLKYU0KS09"
    private val tenpayCode = "f2f00-2YC_1Sfo3jM1G--Zj8kC2Z7koDXC8r"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_donate, container, false)
    }

    override fun onStart() {
        super.onStart()
        donate_alipay.setOnClickListener { view ->
            if (!AlipayUtil.hasInstalledAlipayClient(view.context)) {
                Toast.makeText(
                        view.context, R.string.prompt_alipay_not_found, Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            Toast.makeText(view.context, R.string.prompt_wait, Toast.LENGTH_SHORT).show()
            AlipayUtil.startAlipayClient(view.context, alipayCode)
        }
        donate_tenpay.setOnClickListener { view ->
            val pkg = "com.tencent.mm"
            val componentName = ComponentName(pkg, "$pkg.plugin.base.stub.WXCustomSchemeEntryActivity")
            try {
                view.context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                    component = componentName
                    data = Uri.parse("weixin://magician/donate/$tenpayCode")
                    flags = Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                })
                Toast.makeText(view.context, R.string.prompt_wait, Toast.LENGTH_SHORT).show()
            } catch (e: Throwable) {
                Toast.makeText(view.context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(): DonateFragment {
            return DonateFragment()
        }
    }
}
