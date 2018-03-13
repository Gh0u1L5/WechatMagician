package com.gh0u1l5.wechatmagician.spellbook.interfaces

import android.app.Activity
import android.os.Bundle
import android.view.Menu

interface IActivityHook {

    /**
     * Called when a Wechat MMActivity has created a options menu.
     *
     * @param activity The current activity shown in foreground.
     * @param menu The options menu just created by the activity.
     */
    fun onMMActivityOptionsMenuCreated(activity: Activity, menu: Menu) { }

    /**
     * Called before an activity is created.
     *
     * @param activity The activity object that is creating.
     * @param savedInstanceState The saved instance state for restoring the state.
     */
    fun onActivityCreating(activity: Activity, savedInstanceState: Bundle?) { }

    /**
     * Called before an activity is started.
     *
     * @param activity The activity object that is starting.
     */
    fun onActivityStarting(activity: Activity) { }

    /**
     * Called before an activity is resumed.
     *
     * @param activity The activity object that is resuming.
     */
    fun onActivityResuming(activity: Activity) { }
}