package com.gh0u1l5.wechatmagician

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : Activity(), View.OnClickListener {

    private var tvVersion: TextView? = null
    private var tvProj: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        tvVersion = findViewById(R.id.tvVersion)
        tvProj = findViewById(R.id.tvProj)

        tvVersion?.text = packageManager.getPackageInfo(packageName, 0).versionName
        tvProj?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvProj -> openUrl(R.string.view_about_project_github_url)
        }
    }

    private fun openUrl(resId: Int) {
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(resId)))
        startActivity(intent)
    }
}
