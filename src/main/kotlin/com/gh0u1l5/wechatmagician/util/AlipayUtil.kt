package com.gh0u1l5.wechatmagician.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.net.URISyntaxException

// Reference: http://blog.csdn.net/likesyour/article/details/61198577
object AlipayUtil {

    private const val ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone"
    private const val INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
            "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" +
            "%3Dweb-other&_t=1472443966571#Intent;" +
            "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"

    fun hasInstalledAlipayClient(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo(ALIPAY_PACKAGE_NAME, 0) != null
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace(); false
        }
    }

    fun startAlipayClient(context: Context, urlCode: String): Boolean =
            startIntentUrl(context, INTENT_URL_FORMAT.replace("{urlCode}", urlCode))

    private fun startIntentUrl(context: Context, intentFullUrl: String): Boolean {
        return try {
            val intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME)
            context.startActivity(intent); true
        } catch (e: URISyntaxException) {
            e.printStackTrace(); false
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace(); false
        }
    }
}