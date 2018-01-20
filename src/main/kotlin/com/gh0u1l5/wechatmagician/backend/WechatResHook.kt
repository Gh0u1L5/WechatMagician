package com.gh0u1l5.wechatmagician.backend

import android.content.res.XModuleResources
import android.content.res.XModuleResources.createInstance
import android.content.res.XResources
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_RESOURCES
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.callbacks.XC_InitPackageResources

class WechatResHook : IXposedHookZygoteInit, IXposedHookInitPackageResources {

    companion object {
        @Volatile private var MODULE_PATH: String? = null
        @Volatile private var ORIGIN_RES: XResources? = null
        val MODULE_RES: XModuleResources? by lazy {
            val result = try { createInstance(MODULE_PATH, ORIGIN_RES) } catch (t: Throwable) { log(t); null }
            if (result != null) {
                WechatPackage.setStatus(STATUS_FLAG_RESOURCES, true)
            }
            return@lazy result
        }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        MODULE_PATH = startupParam.modulePath
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        ORIGIN_RES = resparam.res
    }
}