package com.gh0u1l5.wechatmagician.backend

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import android.widget.Toast
import com.gh0u1l5.wechatmagician.storage.SnsCache
import com.gh0u1l5.wechatmagician.storage.Strings
import com.gh0u1l5.wechatmagician.util.DownloadUtil.downloadImage
import com.gh0u1l5.wechatmagician.util.DownloadUtil.downloadThumb
import com.gh0u1l5.wechatmagician.util.DownloadUtil.downloadVideo
import com.gh0u1l5.wechatmagician.util.FileUtil.readBytesFromDisk
import de.robv.android.xposed.XposedBridge.log
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

// ForwardAsyncTask is the AsyncTask that downloads SNS contents and invoke SnsUploadUI.
class ForwardAsyncTask(private val snsId: String?, context: Context) : AsyncTask<Void, Void, Throwable?>() {

    private val str = Strings

    private val snsInfo = SnsCache[snsId]
    private val context = WeakReference(context)
    private val storage = Environment.getExternalStorageDirectory().absolutePath + "/WechatMagician"

    override fun doInBackground(vararg params: Void): Throwable? {
        return try {
            if (snsInfo == null) {
                throw Error(str["prompt_sns_invalid"] + "(snsId: $snsId)")
            }
            if (snsInfo.contentUrl != null) {
                if (snsInfo.medias.isNotEmpty()) {
                    val media = snsInfo.medias[0]
                    downloadThumb("$storage/.cache/0.thumb", media.thumb)
                }
                return null
            }
            snsInfo.medias.mapIndexed { i, media ->
                thread(start = true) {
                    when(media.type) {
                        "2" -> downloadImage("$storage/.cache/$i", media)
                        "6" -> downloadVideo("$storage/.cache/$i", media)
                    }
                }
            }.forEach { it.join() }; null
        } catch (e: Throwable) { e }
    }

    override fun onPostExecute(result: Throwable?) {
        if (result != null) {
            log("FORWARD => $result")
            Toast.makeText(
                    context.get(), result.localizedMessage, Toast.LENGTH_SHORT
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
        when {
            snsInfo?.contentUrl != null -> {
                val imgbuf = readBytesFromDisk("$storage/.cache/0.thumb")
                intent.putExtra("Ksnsupload_type", 1)
                        .putExtra("Ksnsupload_title", snsInfo.title)
                        .putExtra("Ksnsupload_link", snsInfo.contentUrl)
                        .putExtra("Ksnsupload_imgbuf", imgbuf)
            }
            snsInfo?.medias?.isEmpty() == false -> {
                when (snsInfo.medias[0].type) {
                    "2" -> {
                        intent.putStringArrayListExtra(
                                "sns_kemdia_path_list",
                                ArrayList((0 until snsInfo.medias.size).map {
                                    "$storage/.cache/$it"
                                })
                        )
                        intent.removeExtra("Ksnsupload_type")
                    }
                    "6" -> {
                        intent.putExtra("Ksnsupload_type", 14)
                                .putExtra("sight_md5", snsInfo.medias[0].main?.md5)
                                .putExtra("KSightPath", "$storage/.cache/0")
                                .putExtra("KSightThumbPath", "$storage/.cache/0.thumb")
                    }
                }
            }
        }
        context.get()?.startActivity(intent)
    }
}