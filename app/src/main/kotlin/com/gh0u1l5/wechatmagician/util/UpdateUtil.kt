package com.gh0u1l5.wechatmagician.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.webkit.MimeTypeMap
import com.gh0u1l5.wechatmagician.BuildConfig
import com.gh0u1l5.wechatmagician.Global.MAGICIAN_FILE_PROVIDER
import com.gh0u1l5.wechatmagician.R
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpDownload
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.io.File

object UpdateUtil {
    private const val TAG = "UpdateUtil"

    private const val UPDATE_SERVER  = "http://www.gh0u1l5.me/wechat-magician"
    private const val UPDATE_JSON    = "$UPDATE_SERVER/update.json"

    fun checkVersion(activity: Activity) {
//      {
//        "version_code": 51,
//        "address": "./apks/WechatMagician-2.8.1.apk",
//        "description": "Test"
//      }
        UPDATE_JSON.httpGet().responseJson { _, _, result ->
            if (result is Result.Failure) {
                return@responseJson
            }
            val version = result.get().obj()
            val versionCode = version["version_code"] as Int
            if (versionCode > BuildConfig.VERSION_CODE) {
                val apkAddress = "$UPDATE_SERVER/${version["address"]}"
                val description = version["description"] as String
                requireUpdate(activity, apkAddress, description)
            }
        }
    }

    private fun requireUpdate(activity: Activity, apkAddress: String, description: String) {
        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.prompt_update_discovered)
                    .setMessage(description)
                    .setPositiveButton(R.string.button_update, { dialog, _ ->
                        downloadAndInstallApk(activity, apkAddress)
                        dialog.dismiss()
                    })
                    .setNegativeButton(R.string.button_cancel, { dialog, _ ->
                        dialog.dismiss()
                    })
                    .show()
        }
    }

    private fun downloadAndInstallApk(context: Context, apkAddress: String) {
        val apkFile = File.createTempFile("temp", ".apk", context.cacheDir)
        apkAddress.httpDownload().destination { _, _ -> apkFile }.response { _, _, result ->
            if (result is Result.Success) {
                installApk(context, apkFile)
            }
        }
    }

    private fun installApk(context: Context, apk: File) {
        try {
            context.startActivity(Intent().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    action = Intent.ACTION_INSTALL_PACKAGE
                    data = FileProvider.getUriForFile(context, MAGICIAN_FILE_PROVIDER, apk)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                } else {
                    action = Intent.ACTION_VIEW
                    data = Uri.fromFile(apk)
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            })
        } catch (t: Throwable) {
            Log.w(TAG, t)
        }
    }
}