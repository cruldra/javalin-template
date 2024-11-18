package ai_startup_mentor

data class Properties(
    val weiChat: WeiChat
) {
    data class WeiChat(
        val appId: String,
        val appSecret: String,
    )
}

data class WechatLoginData(
    val code: String,
    val encryptedData: String,
    val iv: String
)

data class WechatLoginResult(
    val token: String,
    val userInfo: UserInfo
)

data class UserInfo(
    val avatar: String,
    val nickname: String,
    val phoneNumber: String? = null
)