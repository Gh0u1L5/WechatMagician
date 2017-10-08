package com.gh0u1l5.wechatmagician.backend

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import android.widget.Toast
import com.gh0u1l5.wechatmagician.storage.LocalizedResources
import com.gh0u1l5.wechatmagician.storage.SnsCache
import com.gh0u1l5.wechatmagician.util.DownloadUtil
import de.robv.android.xposed.XposedBridge.log
import java.lang.ref.WeakReference

// ForwardAsyncTask is the AsyncTask that downloads SNS contents and invoke SnsUploadUI.
class ForwardAsyncTask(private val snsId: String?, context: Context) : AsyncTask<Void, Void, Throwable?>() {

    private val res = LocalizedResources

    private val snsInfo = SnsCache[snsId]
    private val context = WeakReference(context)
    private val storage = Environment.getExternalStorageDirectory().path + "/WechatMagician"

    override fun doInBackground(vararg params: Void): Throwable? {
        return try {
            if (snsInfo == null) {
                throw Error(res["prompt_sns_invalid"] + "(snsId: $snsId)")
            }
            snsInfo.medias.forEachIndexed { i, media ->
                when(media.type) {
                    "2" -> DownloadUtil.downloadImage("$storage/.cache/$i", media)
                    "6" -> DownloadUtil.downloadVideo("$storage/.cache/$i", media)
                }
            }; null
        } catch (e: Throwable) { e }
    }

    override fun onPostExecute(result: Throwable?) {
        if (result != null) {
            log("FORWARD => $result")
            Toast.makeText(
                    context.get(), result.toString(), Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (WechatPackage.SnsUploadUI == null) {
            return
        }

        val intent = Intent(context.get(), WechatPackage.SnsUploadUI)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("Ksnsforward", true)
                .putExtra("Ksnsupload_type", 9)
                .putExtra("Kdescription", snsInfo?.content)
        if (snsInfo?.medias?.isEmpty() == false) {
            if (snsInfo.medias[0].type == "6") {
                intent.putExtra("Ksnsupload_type", 14)
                        .putExtra("sight_md5", snsInfo.medias[0].main?.md5)
                        .putExtra("KSightPath", "$storage/.cache/0")
                        .putExtra("KSightThumbPath", "$storage/.cache/0.thumb")
            } else {
                intent.putStringArrayListExtra(
                        "sns_kemdia_path_list",
                        ArrayList((0 until snsInfo.medias.size).map {
                            "$storage/.cache/$it"
                        })
                )
                intent.removeExtra("Ksnsupload_type")
            }
        }
        context.get()?.startActivity(intent)
    }
}