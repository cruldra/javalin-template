package ai_startup_mentor

import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl
import io.ebean.Database
import io.ebean.DatabaseFactory
import io.ebean.config.DatabaseConfig
import io.ebean.datasource.DataSourceConfig
import jzeus.ini.asIniFile
import jzeus.ini.toJavaObject
import jzeus.ioc.adapter
import jzeus.json.objectMapper
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
    IOC.addComponent(configDatabase())
}

fun configDatabase(): Database {
    val properties = IOC.getComponent(Properties::class.java)
    val config = DatabaseConfig()
    config.objectMapper = objectMapper
    config.setDataSourceConfig(properties.db)
    return DatabaseFactory.create(config)
}


fun main() {
    val dataSourceConfig = DataSourceConfig()
    dataSourceConfig.setUrl("jdbc:postgresql://192.168.1.3:5432/ai_startup_mentor")
    dataSourceConfig.setUsername("DATABASE_USERNAME")
    dataSourceConfig.setPassword("DATABASE_PASSWORD")
    dataSourceConfig.setDriver("org.mariadb.jdbc.Driver")
    val config = DatabaseConfig()
    config.objectMapper = objectMapper
    config.setDataSourceConfig(dataSourceConfig)
      DatabaseFactory.create(config)
}
