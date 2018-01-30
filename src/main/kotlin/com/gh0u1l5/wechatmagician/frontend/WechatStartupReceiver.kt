package com.gh0u1l5.wechatmagician.frontend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gh0u1l5.wechatmagician.Global.ACTION_UPDATE_PREF
import com.gh0u1l5.wechatmagician.Global.ACTION_WECHAT_STARTUP
import com.gh0u1l5.wechatmagician.Global.FOLDER_SHARED_PREFS
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_BASE_DIR
import com.gh0u1l5.wechatmagician.util.FileUtil
import java.io.File

// WechatStartupReceiver will fix the file permissions for shared preferences.
class WechatStartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_WECHAT_STARTUP) {
            val baseDir = File(MAGICIAN_BASE_DIR)
            FileUtil.setWorldExecutable(baseDir)

            val sharedPrefsDir = File(baseDir, FOLDER_SHARED_PREFS)
            FileUtil.setWorldExecutable(sharedPrefsDir)

            sharedPrefsDir.listFiles().forEach { file ->
                FileUtil.setWorldReadable(file)
            }

            context.sendBroadcast(Intent().setAction(ACTION_UPDATE_PREF))
        }
    }
}