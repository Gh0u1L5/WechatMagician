package com.gh0u1l5.wechatmagician

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import android.widget.Toast
import com.gh0u1l5.wechatmagician.util.ImageUtil
import com.gh0u1l5.wechatmagician.xposed.SnsCache.SnsInfo
import com.gh0u1l5.wechatmagician.xposed.WechatPackage
import de.robv.android.xposed.XposedBridge.log


class ForwardAsyncTask(private val snsInfo: SnsInfo, private val context: Context) : AsyncTask<Void, Void, Throwable?>() {

    private val storage = Environment.getExternalStorageDirectory().path + "/WechatMagician"

    override fun doInBackground(vararg params: Void): Throwable? {
        return try {
            snsInfo.medias.forEachIndexed { i, media ->
                ImageUtil.downloadImage("$storage/.cache/$i", media)
            }; null
        } catch (e: Throwable) { e }
    }

    override fun onPostExecute(result: Throwable?) {
        if (result != null) {
            log("DOWNLOAD => $result")
            Toast.makeText(
                    context, result.toString(), Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (WechatPackage.SnsUploadUI == null) {
            return
        }

        val intent = Intent(context, WechatPackage.SnsUploadUI)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                .putExtra("Kdescription", snsInfo.content)
                .putExtra("Ksnsupload_type", 9)
        if (snsInfo.medias.isNotEmpty()) {
            intent.putStringArrayListExtra(
                    "sns_kemdia_path_list",
                    ArrayList((0 until snsInfo.medias.size).map {
                        "$storage/.cache/$it"
                    })
            )
            intent.removeExtra("Ksnsupload_type")
        }
        context.startActivity(intent)
    }
}