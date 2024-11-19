package ai_startup_mentor

import io.ebean.datasource.DataSourceConfig

data class Properties(
    val weiChat: WeiChat,
    val db: DataSourceConfig,
    val server: Server,
) {
    data class WeiChat(
        val appId: String,
        val appSecret: String,
    )

    data class Server(val host: String)
}

data class AccountLoginData(
    val username: String,
    val password: String
)

data class WechatLoginData(
    val code: String,
    val encryptedData: String,
    val iv: String
)

data class LoginResult(
    val token: String,
    val userInfo: UserInfo
)

data class UserInfo(
    val id: Long,
    val avatar: String,
    val phoneNumber: String? = null
)
