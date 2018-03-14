import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal
import com.gh0u1l5.wechatmagician.spellbook.base.Version
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil
import dalvik.system.PathClassLoader
import net.dongliu.apk.parser.ApkFile
import org.junit.Test as Test

class MirrorUnitTest {
    private fun loadWechatApk(apkPath: String) {
        WechatGlobal.wxUnitTestMode = true
        var apkFile: ApkFile? = null
        try {
            apkFile = ApkFile(apkPath)
            WechatGlobal.wxVersion = Version(apkFile.apkMeta.versionName)
            WechatGlobal.wxPackageName = apkFile.apkMeta.packageName
            WechatGlobal.wxLoader = PathClassLoader(apkPath, ClassLoader.getSystemClassLoader())
            WechatGlobal.wxClasses = apkFile.dexClasses.map { clazz ->
                ReflectionUtil.getClassName(clazz)
            }
        } finally {
            apkFile?.close()
        }
    }

    @Test fun testWechatSignatures() {

    }
}