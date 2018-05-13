package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import android.view.*
import android.widget.AdapterView
import android.widget.ListPopupWindow
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.backend.storage.Strings
import com.gh0u1l5.wechatmagician.backend.storage.cache.SnsCache
import com.gh0u1l5.wechatmagician.frontend.wechat.ListPopupWindowPosition
import com.gh0u1l5.wechatmagician.frontend.wechat.StringListAdapter
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.SnsUserUIAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.base.Operation.Companion.nop
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IXmlParserHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.plugin.sns.ui.Classes.SnsTimeLineUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.plugin.sns.ui.Classes.SnsUploadUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.plugin.sns.ui.Classes.SnsUserUI
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.plugin.sns.ui.Fields.SnsUploadUI_mSnsEditText
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryAsynchronously
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryVerbosely
import com.gh0u1l5.wechatmagician.spellbook.util.FileUtil
import com.gh0u1l5.wechatmagician.util.DownloadUtil
import com.gh0u1l5.wechatmagician.util.ImageUtil
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.ViewUtil
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px
import com.gh0u1l5.wechatmagician.util.ViewUtil.getListViewFromSnsActivity
import com.gh0u1l5.wechatmagician.util.ViewUtil.getViewAtPosition
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getLongField
import java.lang.ref.WeakReference

object SnsForward : IActivityHook, IAdapterHook, IDatabaseHook, IXmlParserHook {

    // ForwardAsyncTask is the AsyncTask that downloads SNS contents and invoke SnsUploadUI.
    class ForwardAsyncTask(snsId: Long, context: Context) : AsyncTask<Void, Void, Throwable?>() {

        private val snsId = MessageUtil.longToDecimalString(snsId)
        private val snsInfo = SnsCache[this.snsId]
        private val context = WeakReference(context)
        private val storage = Environment.getExternalStorageDirectory().absolutePath + "/WechatMagician"

        override fun doInBackground(vararg params: Void): Throwable? {
            return try {
                val state = Environment.getExternalStorageState()
                if (state != MEDIA_MOUNTED) {
                    throw Error("SD card is not presented! (state: $state)")
                }
                if (snsInfo == null) {
                    val prompt = Strings.getString(R.string.prompt_sns_invalid)
                    throw Error("$prompt (snsId: $snsId)")
                }
                if (snsInfo.contentUrl != null) {
                    if (snsInfo.medias.isNotEmpty()) {
                        val media = snsInfo.medias[0]
                        DownloadUtil.downloadThumb("$storage/.cache/0.thumb", media.thumb)
                    }
                    return null
                }
                snsInfo.medias.mapIndexed { i, media ->
                    tryAsynchronously {
                        when (media.type) {
                            "2" -> DownloadUtil.downloadImage("$storage/.cache/$i", media)
                            "6" -> DownloadUtil.downloadVideo("$storage/.cache/$i", media)
                        }
                    }
                }.forEach(Thread::join); null
            } catch (t: Throwable) { t }
        }

        override fun onPostExecute(result: Throwable?) {
            if (result != null) {
                val message = result.localizedMessage
                Toast.makeText(context.get(), message, Toast.LENGTH_LONG).show()
                return
            }

            val intent = Intent(context.get(), SnsUploadUI)
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

    private const val ROOT_TAG = "TimelineObject"
    private const val ID_TAG   = ".TimelineObject.id"

    override fun onDatabaseOpened(path: String, factory: Any?, flags: Int, errorHandler: Any?, result: Any?): Operation<Any> {
        if (result == null) {
            return nop()
        }
        if (path.endsWith("SnsMicroMsg.db")) {
            // Force Wechat to retrieve existing SNS data from remote server.
            val deleted = ContentValues().apply { put("sourceType", 0) }
            tryVerbosely {
                callMethod(result, "delete", "snsExtInfo3", "local_flag=0", null)
                callMethod(result, "update", "SnsInfo", deleted, "sourceType in (8,10,12,14)", null)
            }
        }
        return nop()
    }

    override fun onXmlParsed(xml: String, root: String, result: MutableMap<String, String>) {
        tryAsynchronously {
            if (root == ROOT_TAG) {
                val id = result[ID_TAG]
                if (id != null) {
                    SnsCache[id] = SnsCache.SnsInfo(result)
                }
            }
        }
    }

    override fun onHeaderViewListAdapterGotView(adapter: Any, position: Int, convertView: View?, parent: ViewGroup, result: View?): Operation<View> {
        if (result == null) {
            return nop()
        }
        if (adapter === SnsUserUIAdapterObject.get()) {
            if (convertView == null) { // this is a new view
                if (result is ViewGroup) {
                    repeat(result.childCount, {
                        result.getChildAt(it).isClickable = false
                    })
                }
                result.isLongClickable = true
            }
        }
        return nop()
    }

    override fun onActivityStarting(activity: Activity) {
        when (activity::class.java) {
            SnsUserUI -> {
                // Hook SnsUserUI to popup a menu during long click.
                val listView = getListViewFromSnsActivity(activity) ?: return
                SnsUserUIAdapterObject = WeakReference(listView.adapter)
                listView.setOnItemLongClickListener { parent, view, position, _ ->
                    val item = parent.getItemAtPosition(position)
                    val snsId = getLongField(item, "field_snsId")
                    onTimelineItemLongClick(parent, view, snsId, null)
                }
            }
            SnsTimeLineUI -> {
                // Hook SnsTimeLineUI to popup a menu during long click.
                val listView = getListViewFromSnsActivity(activity) ?: return
                // Set onTouchListener for the list view.
                var lastKnownX = 0
                var lastKnownY = 0
                val detector = GestureDetector(listView.context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent?) {
                        val position = listView.pointToPosition(lastKnownX, lastKnownY)
                        val view = listView.getViewAtPosition(position)
                        val item = listView.getItemAtPosition(position)
                        val snsId = getLongField(item, "field_snsId")
                        val popup = ListPopupWindowPosition(listView, lastKnownX, lastKnownY)
                        onTimelineItemLongClick(listView, view, snsId, popup)
                    }
                })
                (listView as View).setOnTouchListener { _, event ->
                    lastKnownX = event.x.toInt()
                    lastKnownY = event.y.toInt()
                    return@setOnTouchListener detector.onTouchEvent(event)
                }
            }
            SnsUploadUI -> {
                // Hook SnsUploadUI to clean EditText properly before forwarding.
                val intent = activity.intent ?: return
                if (intent.getBooleanExtra("Ksnsforward", false)) {
                    val content = intent.getStringExtra("Kdescription")
                    val editText = SnsUploadUI_mSnsEditText.get(activity)
                    callMethod(editText, "setText", content)
                }
            }
        }
    }

    // Show a popup menu in SnsTimelineUI
    private fun onTimelineItemLongClick(parent: AdapterView<*>, view: View, snsId: Long, position: ListPopupWindowPosition?): Boolean {
        val textForward = Strings.getString(R.string.button_sns_forward)
        val textScreenshot = Strings.getString(R.string.button_sns_screenshot)
        ListPopupWindow(parent.context).apply {
            if (position != null) {
                // Calculate list view size
                val location = IntArray(2)
                position.anchor.getLocationOnScreen(location)
                val bottom = location[1] + position.anchor.height

                // Set position for popup window
                anchorView = position.anchor
                horizontalOffset = position.x - position.anchor.left
                verticalOffset = position.y - bottom
            } else {
                anchorView = view
            }

            // Set general properties for popup window
            width = parent.context.dp2px(120)
            setDropDownGravity(Gravity.CENTER)
            setAdapter(StringListAdapter(view.context, listOf(textForward, textScreenshot)))
            setOnItemClickListener { _, _, operation, _ ->
                onTimelineItemPopupMenuSelected(view, snsId, operation)
                dismiss()
            }
        }.show()
        return true
    }

    // Handle the logic about the popup menu in SnsTimelineUI
    private fun onTimelineItemPopupMenuSelected(itemView: View, snsId: Long, operation: Int): Boolean {
        val promptWait = Strings.getString(R.string.prompt_wait)
        val promptScreenshot = Strings.getString(R.string.prompt_screenshot)
        when (operation) {
            0 -> { // Forward
                ForwardAsyncTask(snsId, itemView.context).execute()
                Toast.makeText(itemView.context, promptWait, Toast.LENGTH_SHORT).show()
                return true
            }
            1 -> { // Screenshot
                val context = itemView.context ?: return true
                try {
                    val path = ImageUtil.createScreenshotPath()
                    val bitmap = ViewUtil.drawView(itemView)
                    FileUtil.writeBitmapToDisk(path, bitmap)
                    FileUtil.notifyNewMediaFile(path, itemView.context)
                    Toast.makeText(context, "$promptScreenshot $path", LENGTH_SHORT).show()
                } catch (t: Throwable) {
                    Toast.makeText(context, "Error: $t", LENGTH_SHORT).show()
                }
                return true
            }
        }
        return false
    }
}