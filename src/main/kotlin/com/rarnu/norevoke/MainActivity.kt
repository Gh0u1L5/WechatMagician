package com.rarnu.norevoke

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    private var tvVersion: TextView? = null
    private var tvIntro: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        tvVersion = findViewById(R.id.tvVersion) as TextView?
        tvIntro = findViewById(R.id.tvIntro) as TextView?

    }
}
