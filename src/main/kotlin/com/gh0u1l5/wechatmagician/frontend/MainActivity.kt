package com.gh0u1l5.wechatmagician.frontend

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.frontend.fragments.DonateFragment
import com.gh0u1l5.wechatmagician.frontend.fragments.PrefFragment
import com.gh0u1l5.wechatmagician.frontend.fragments.StatusFragment
import com.gh0u1l5.wechatmagician.frontend.fragments.SupportFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : Activity(),
        NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 0)
            } else {
                loadHomeFragment()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != PERMISSION_GRANTED) {
            finish()
        }
        loadHomeFragment()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(Gravity.START)) {
            drawer_layout.closeDrawer(Gravity.START)
        } else {
            super.onBackPressed()
        }
    }

    fun onGithubLinkClick(view: View?) {
        val url = Uri.parse(view?.context?.getString(R.string.view_about_project_github_url))
        view?.context?.startActivity(Intent(Intent.ACTION_VIEW).setData(url))
    }

    private fun loadHomeFragment() {
        findViewById<ConstraintLayout?>(R.id.main_container) ?: return
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, StatusFragment.newInstance())
                .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        findViewById<ConstraintLayout?>(R.id.main_container) ?: return false
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_status -> {
                StatusFragment.newInstance()
            }
            R.id.nav_settings -> {
                PrefFragment.newInstance(
                        R.xml.pref_settings, "settings",
                        hashMapOf("settings_sns_keyword_blacklist_content" to true))
            }
            R.id.nav_developer -> {
                PrefFragment.newInstance(R.xml.pref_developer, "developer")
            }
            R.id.nav_support -> {
                SupportFragment.newInstance()
            }
            R.id.nav_donate -> {
                DonateFragment.newInstance()
            }
            else ->
                throw Error("Unknown navigation item: ${item.itemId}")
        }
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit()

        drawer_layout.closeDrawer(Gravity.START)
        return true
    }
}
