package com.rarnu.norevoke

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : Activity(), View.OnClickListener {

    private var tvVersion: TextView? = null
    private var tvProj: TextView? = null
    private var tvRepo1: TextView? = null
    private var tvRepo2: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        tvVersion = findViewById(R.id.tvVersion) as TextView?
        tvProj = findViewById(R.id.tvProj) as TextView?
        tvRepo1 = findViewById(R.id.tvRepo1) as TextView?
        tvRepo2 = findViewById(R.id.tvRepo2) as TextView?

        try {
            tvVersion?.text = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {

        }
        tvProj?.setOnClickListener(this)
        tvRepo1?.setOnClickListener(this)
        tvRepo2?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvProj -> openUrl(R.string.view_about_project_github_url)
            R.id.tvRepo1 -> openUrl(R.string.view_about_project_repo1_url)
            R.id.tvRepo2 -> openUrl(R.string.view_about_project_repo2_url)
        }
    }

    private fun openUrl(resId: Int) {
        val u = Uri.parse(getString(resId))
        val inWeb = Intent(Intent.ACTION_VIEW)
        inWeb.data = u
        startActivity(inWeb)
    }
}
