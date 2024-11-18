package ai_startup_mentor

import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl
import jzeus.ini.asIniFile
import jzeus.ini.toJavaObject
import jzeus.ioc.adapter
import org.picocontainer.DefaultPicoContainer

val IOC = DefaultPicoContainer()
fun configIOC() {
    IOC.adapter {
        "app.ini".asIniFile().toJavaObject(Properties::class.java)
    }
    IOC.adapter {
        val properties = IOC.getComponent(Properties::class.java)
        WxMaServiceImpl().apply {
            wxMaConfig = WxMaDefaultConfigImpl().apply {
                appid = properties.weiChat.appId
                secret = properties.weiChat.appSecret
            }
        }
    }
}
