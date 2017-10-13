package com.gh0u1l5.wechatmagician.frontend.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gh0u1l5.wechatmagician.R

class DonateFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_donate, container, false)
    }

    companion object {
        fun newInstance(): DonateFragment {
            return DonateFragment()
        }
    }
}
