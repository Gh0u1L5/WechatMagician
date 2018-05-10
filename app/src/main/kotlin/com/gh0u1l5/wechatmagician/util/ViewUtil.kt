package com.gh0u1l5.wechatmagician.util

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.plugin.sns.ui.Classes.SnsActivity
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType

// ViewUtil is a helper object for debugging / handling UI operations.
object ViewUtil {
    private const val TAG = "ViewUtil"

    // dumpViewGroup dumps the structure of a view group.
    fun dumpViewGroup(prefix: String, viewGroup: ViewGroup) {
        repeat(viewGroup.childCount, {
            var attrs = mapOf<String, Any?>()
            val child = viewGroup.getChildAt(it)

            val getAttr = {getter: String ->
                if (child::class.java.methods.count{ it.name == getter } != 0) {
                    attrs += getter to XposedHelpers.callMethod(child, getter)
                }
            }
            getAttr("getWidth")
            getAttr("getHeight")
            getAttr("getTag")
            getAttr("getText")
            getAttr("isClickable")
            getAttr("isLongClickable")

            XposedBridge.log("$prefix[$it] => ${child::class.java}, $attrs")
            if (child is ViewGroup) {
                dumpViewGroup("$prefix[$it]", child)
            }
        })
    }

    // searchViewGroup returns the first view that matches a specific class name in the given view group.
    fun searchViewGroup(viewGroup: ViewGroup, className: String): View? {
        repeat(viewGroup.childCount, {
            val child = viewGroup.getChildAt(it)
            if (child::class.java.name == className) {
                return child
            }
            if (child is ViewGroup) {
                val result = searchViewGroup(child, className)
                if (result != null) {
                    return result
                }
            }
        })
        return null
    }

    // drawView draws the content of a view to a bitmap.
    fun drawView(view: View): Bitmap {
        val width = view.width
        val height = view.height
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        b.eraseColor(Color.WHITE)
        view.draw(Canvas(b))
        return b
    }

    // openURL opens an URL using an external explorer.
    fun openURL(context: Context?, url: String?) {
        try {
            context?.startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))
        } catch (t: Throwable) {
            Log.e(TAG, "Cannot open URL $url: $t")
            Toast.makeText(context, t.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // Context.dp2px convert size in dp to size in px.
    fun Context.dp2px(dip: Int): Int {
        val scale = resources.displayMetrics.density
        return (dip * scale + 0.5f).toInt()
    }

    // Resources.getDefaultLanguage returns current default language for the given resources
    @Suppress("DEPRECATION")
    fun Resources.getDefaultLanguage(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0]
        } else {
            configuration.locale
        }.language
    }

    // ListView.getViewAtPosition returns the item view at specific position
    fun ListView.getViewAtPosition(position: Int): View {
        val firstItemPosition = firstVisiblePosition
        val lastItemPosition = firstItemPosition + childCount - 1

        return if (position < firstItemPosition || position > lastItemPosition) {
            adapter.getView(position, null, this)
        } else {
            getChildAt(position - firstItemPosition)
        }
    }

    // getListViewFromSnsActivity takes the ListView of a SnsActivity out of its container.
    fun getListViewFromSnsActivity(container: Any?): ListView? {
        if (container == null) {
            return null
        }

        val activityField = findFirstFieldByExactType(container::class.java, SnsActivity)
        val activity = activityField.get(container)
        val listViewField = findFirstFieldByExactType(activity::class.java, ListView::class.java)
        return listViewField.get(activity) as ListView
    }
}