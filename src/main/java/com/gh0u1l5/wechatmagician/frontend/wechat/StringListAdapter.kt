package com.gh0u1l5.wechatmagician.frontend.wechat

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px
import de.robv.android.xposed.XposedHelpers

class StringListAdapter(context: Context, strings: List<String>) :
        ArrayAdapter<String>(context, 0, strings) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView as LinearLayout?
        if (view == null) {
            val containerLayout = XposedHelpers.callMethod(parent, "generateDefaultLayoutParams")
            XposedHelpers.setIntField(containerLayout, "height", context.dp2px(50))
            view = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                if (containerLayout is ViewGroup.LayoutParams) {
                    layoutParams = containerLayout
                }

                val textLayout = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                textLayout.marginStart = context.dp2px(15)
                textLayout.marginEnd = context.dp2px(15)
                addView(TextView(context).apply {
                    textSize = 16F
                    gravity = Gravity.CENTER_VERTICAL
                    setSingleLine()
                    setTextColor(Color.BLACK)
                }, textLayout)
                setBackgroundColor(Color.WHITE)
            }
        }
        return view.apply {
            val textView = getChildAt(0) as TextView?
            textView?.text = getItem(position)
        }
    }
}