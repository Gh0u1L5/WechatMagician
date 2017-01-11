package com.rarnu.norevoke

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    private var tvVersion: TextView? = null
    private var tvProj: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        tvVersion = findViewById(R.id.tvVersion) as TextView?
        tvProj = findViewById(R.id.tvProj) as TextView?

        try {
            tvVersion?.text = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {

        }
        tvProj?.setOnClickListener {
            val inGithub = Intent(Intent.ACTION_VIEW)
            inGithub.data = Uri.parse("https://github.com/rarnu/wechat_no_revoke")
            startActivity(inGithub)
        }
    }
}
