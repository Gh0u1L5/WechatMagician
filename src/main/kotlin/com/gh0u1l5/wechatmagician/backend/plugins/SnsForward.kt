package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import android.view.*
import android.widget.AdapterView
import android.widget.ListPopupWindow
import android.widget.Toast
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.MENU_SNS_FORWARD
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.MENU_SNS_SCREENSHOT
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.PROMPT_SCREENSHOT
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.PROMPT_SNS_INVALID
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.PROMPT_WAIT
import com.gh0u1l5.wechatmagician.backend.storage.cache.SnsCache
import com.gh0u1l5.wechatmagician.frontend.wechat.ListPopupWindowPosition
import com.gh0u1l5.wechatmagician.frontend.wechat.StringListAdapter
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SnsUploadUI
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SnsUploadUIEditTextField
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SnsUserUIAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IXmlParserHook
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryAsynchronously
import com.gh0u1l5.wechatmagician.util.*
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px
import com.gh0u1l5.wechatmagician.util.ViewUtil.getListViewFromSnsActivity
import com.gh0u1l5.wechatmagician.util.ViewUtil.getViewAtPosition
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.*
import java.lang.ref.WeakReference

object SnsForward : IActivityHook, IAdapterHook, IDatabaseHook, IXmlParserHook {

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
                    throw Error(str[PROMPT_SNS_INVALID] + "(snsId: $snsId)")
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
                        when(media.type) {
                            "2" -> DownloadUtil.downloadImage("$storage/.cache/$i", media)
                            "6" -> DownloadUtil.downloadVideo("$storage/.cache/$i", media)
                        }
                    }
                }.forEach(Thread::join); null
            } catch (t: Throwable) { t }
        }

        override fun onPostExecute(result: Throwable?) {
            if (result != null) {
                log("FORWARD => $result")
                Toast.makeText(
                        context.get(), result.localizedMessage, Toast.LENGTH_SHORT
                ).show()
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

    private val str = LocalizedStrings

    override fun onDatabaseOpened(path: String, database: Any) {
        if (path.endsWith("SnsMicroMsg.db")) {
            // Force Wechat to retrieve existing SNS data from remote server.
            val deleted = ContentValues().apply { put("sourceType", 0) }
            callMethod(database, "delete", "snsExtInfo3", "local_flag=0", null)
            callMethod(database, "update", "SnsInfo", deleted, "sourceType in (8,10,12,14)", null)
        }
    }

    override fun onXmlParsed(root: String, xml: MutableMap<String, String>) {
        tryAsynchronously {
            if (root == ROOT_TAG) {
                val id = xml[ID_TAG]
                if (id != null) {
                    SnsCache[id] = SnsCache.SnsInfo(xml)
                }
            }
        }
    }

    // Hook HeaderViewListAdapter.getView to make sure the items are long clickable.
    override fun onSnsUserUIAdapterGotView(adapter: Any, convertView: View?, view: View) {
        if (adapter === SnsUserUIAdapterObject.get()) {
            if (convertView == null) { // this is a new view
                if (view is ViewGroup) {
                    repeat(view.childCount, {
                        view.getChildAt(it).isClickable = false
                    })
                }
                view.isLongClickable = true
            }
        }
    }

    // Hook SnsUserUI.onCreate to popup a menu during long click.
    override fun onSnsUserUICreated(activity: Activity) {
        val listView = getListViewFromSnsActivity(activity) ?: return
        SnsUserUIAdapterObject = WeakReference(listView.adapter)
        listView.setOnItemLongClickListener { parent, view, position, _ ->
            val item = parent.getItemAtPosition(position)
            val snsId = getLongField(item, "field_snsId")
            onTimelineItemLongClick(parent, view, snsId, null)
        }
    }

    // Hook SnsTimeLineUI.onCreate to popup a menu during long click.
    override fun onSnsTimelineUICreated(activity: Activity) {
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

    // Hook SnsUploadUI.onCreate to clean EditText properly before forwarding.
    override fun onSnsUploadUICreated(activity: Activity) {
        val intent = activity.intent ?: return
        if (intent.getBooleanExtra("Ksnsforward", false)) {
            val content = intent.getStringExtra("Kdescription")
            val editTextField = SnsUploadUIEditTextField
            val editText = getObjectField(activity, editTextField)
            callMethod(editText, "setText", content)
        }
    }

    // Show a popup menu in SnsTimelineUI
    private fun onTimelineItemLongClick(parent: AdapterView<*>, view: View, snsId: Long, position: ListPopupWindowPosition?): Boolean {
        val operations = listOf(str[MENU_SNS_FORWARD], str[MENU_SNS_SCREENSHOT])
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
            setAdapter(StringListAdapter(view.context, operations))
            setOnItemClickListener { _, _, operation, _ ->
                onTimelineItemPopupMenuSelected(view, snsId, operation)
                dismiss()
            }
        }.show()
        return true
    }

    // Handle the logic about the popup menu in SnsTimelineUI
    private fun onTimelineItemPopupMenuSelected(itemView: View, snsId: Long, operation: Int): Boolean {
        when (operation) {
            0 -> { // Forward
                ForwardAsyncTask(snsId, itemView.context).execute()
                Toast.makeText(
                        itemView.context, str[PROMPT_WAIT], Toast.LENGTH_SHORT
                ).show()
                return true
            }
            1 -> { // Screenshot
                val path = ImageUtil.createScreenshotPath()
                val bitmap = ViewUtil.drawView(itemView)
                FileUtil.writeBitmapToDisk(path, bitmap)
                FileUtil.notifyNewMediaFile(path, itemView.context)
                Toast.makeText(
                        itemView.context, str[PROMPT_SCREENSHOT] + path, Toast.LENGTH_SHORT
                ).show()
                return true
            }
        }
        return false
    }
}