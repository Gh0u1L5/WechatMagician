package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatEvents
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.frontend.wechat.ListPopupPosition
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.cache.SnsCache
import com.gh0u1l5.wechatmagician.util.DownloadUtil
import com.gh0u1l5.wechatmagician.util.FileUtil
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.ViewUtil.getListViewFromSnsActivity
import com.gh0u1l5.wechatmagician.util.ViewUtil.getViewAtPosition
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.*
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

object SnsForward {

    private val pkg = WechatPackage
    private val events = WechatEvents

    // ForwardAsyncTask is the AsyncTask that downloads SNS contents and invoke SnsUploadUI.
    class ForwardAsyncTask(snsId: Long, context: Context) : AsyncTask<Void, Void, Throwable?>() {

        private val str = LocalizedStrings

        private val snsId = MessageUtil.longToDecimalString(snsId)
        private val snsInfo = SnsCache[this.snsId]
        private val context = WeakReference(context)
        private val storage = Environment.getExternalStorageDirectory().absolutePath + "/WechatMagician"

        override fun doInBackground(vararg params: Void): Throwable? {
            return try {
                if (snsInfo == null) {
                    throw Error(str[LocalizedStrings.PROMPT_SNS_INVALID] + "(snsId: $snsId)")
                }
                if (snsInfo.contentUrl != null) {
                    if (snsInfo.medias.isNotEmpty()) {
                        val media = snsInfo.medias[0]
                        DownloadUtil.downloadThumb("$storage/.cache/0.thumb", media.thumb)
                    }
                    return null
                }
                snsInfo.medias.mapIndexed { i, media ->
                    thread(start = true) {
                        when(media.type) {
                            "2" -> DownloadUtil.downloadImage("$storage/.cache/$i", media)
                            "6" -> DownloadUtil.downloadVideo("$storage/.cache/$i", media)
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
            if (pkg.SnsUploadUI == null) {
                return
            }

            val intent = Intent(context.get(), pkg.SnsUploadUI)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("Ksnsforward", true)
                    .putExtra("Ksnsupload_type", 9)
                    .putExtra("Kdescription", snsInfo?.content)
            when {
                snsInfo?.contentUrl != null -> {
                    val buffer = FileUtil.readBytesFromDisk("$storage/.cache/0.thumb")
                    intent.putExtra("Ksnsupload_type", 1)
                            .putExtra("Ksnsupload_title", snsInfo.title)
                            .putExtra("Ksnsupload_link", snsInfo.contentUrl)
                            .putExtra("Ksnsupload_imgbuf", buffer)
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

    // Hook SnsUserUI.onCreate to popup a menu during long click.
    @JvmStatic fun setLongClickListenerForSnsUserUI() {
        if (pkg.SnsUserUI == null) {
            return
        }

        findAndHookMethod(pkg.SnsUserUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val listView = getListViewFromSnsActivity(param.thisObject) ?: return

                // Set onLongClickListener for items
                listView.setOnItemLongClickListener { parent, view, position, _ ->
                    val item = parent.getItemAtPosition(position)
                    val snsId = getLongField(item, "field_snsId")
                    events.onTimelineItemLongClick(parent, view, snsId, null)
                }

                // Hook adapter to make sure the items are long clickable.
                val adapter = listView.adapter ?: return
                XposedHelpers.findAndHookMethod(
                        adapter.javaClass, "getView",
                        C.Int, C.View, C.ViewGroup, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (param.thisObject === adapter) {
                            val convertView = param.args[1] as View?
                            if (convertView == null) { // this is a new view
                                val view = param.result as View? ?: return
                                if (view is ViewGroup) {
                                    repeat(view.childCount, {
                                        view.getChildAt(it).isClickable = false
                                    })
                                }
                                view.isLongClickable = true
                            }
                        }
                    }
                })
            }
        })
    }

    // Hook SnsTimeLineUI.onCreate to popup a menu during long click.
    @JvmStatic fun setLongClickListenerForSnsTimeLineUI() {
        if (pkg.SnsTimeLineUI == null) {
            return
        }

        findAndHookMethod(pkg.SnsTimeLineUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val listView = getListViewFromSnsActivity(param.thisObject) ?: return

                // Set onTouchListener for the list view.
                var lastKnownX = 0
                var lastKnownY = 0
                val detector = GestureDetector(listView.context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent?) {
                        val position = listView.pointToPosition(lastKnownX, lastKnownY)
                        val view = listView.getViewAtPosition(position)
                        val item = listView.getItemAtPosition(position)
                        val snsId = getLongField(item, "field_snsId")
                        val popup = ListPopupPosition(listView, lastKnownX, lastKnownY)
                        events.onTimelineItemLongClick(listView, view, snsId, popup)
                    }
                })
                (listView as View).setOnTouchListener { _, event ->
                    lastKnownX = event.x.toInt()
                    lastKnownY = event.y.toInt()
                    return@setOnTouchListener detector.onTouchEvent(event)
                }
            }
        })
    }

    // Hook SnsUploadUI.onCreate to clean EditText properly before forwarding.
    @JvmStatic fun cleanTextViewBeforeForwarding() {
        when (null) {
            pkg.SnsUploadUI,
            pkg.SnsUploadUIEditTextField -> return
        }

        findAndHookMethod(pkg.SnsUploadUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent ?: return
                if (intent.getBooleanExtra("Ksnsforward", false)) {
                    val content = intent.getStringExtra("Kdescription")
                    val editTextField = pkg.SnsUploadUIEditTextField
                    val editText = getObjectField(param.thisObject, editTextField)
                    XposedHelpers.callMethod(editText, "setText", content)
                }
            }
        })
    }
}