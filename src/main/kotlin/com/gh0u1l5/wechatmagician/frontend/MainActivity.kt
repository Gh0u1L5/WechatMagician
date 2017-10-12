package com.gh0u1l5.wechatmagician.frontend

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import com.gh0u1l5.wechatmagician.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : Activity(),
        NavigationView.OnNavigationItemSelectedListener,
        StatusFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        findViewById<ConstraintLayout?>(R.id.main_container) ?: return
        if (savedInstanceState != null) {
            return // avoid overlapping
        }
        fragmentManager.beginTransaction()
                .add(R.id.main_container, StatusFragment())
                .commit()
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        findViewById<ConstraintLayout?>(R.id.main_container) ?: return false
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_status -> {
                StatusFragment()
            }
            R.id.nav_settings -> {
                PrefFragment.newInstance(R.xml.pref_settings)
            }
            R.id.nav_developer -> {
                PrefFragment.newInstance(R.xml.pref_developer)
            }
            R.id.nav_support -> {
                StatusFragment()
            }
            R.id.nav_donate -> {
                StatusFragment()
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

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
