package com.gh0u1l5.wechatmagician.frontend

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView

class WechatListPopupAdapter(context: Context, strings: List<String>) : ArrayAdapter<String>(context, 0, strings) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView as LinearLayout?
        if (view == null) {
            view = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, 140)

                val params = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                params.setMargins(50, 0, 0, 0)
                addView(TextView(context).apply {
                    textSize = 16F
                    gravity = Gravity.CENTER_VERTICAL
                    setTextColor(Color.BLACK)
                }, params)
                setBackgroundColor(Color.WHITE)
            }
        }
        return view.apply {
            val textView = getChildAt(0) as TextView?
            textView?.text = getItem(position)
        }
    }
}