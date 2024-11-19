package ai_startup_mentor

import io.ebean.datasource.DataSourceConfig
import javax.annotation.meta.TypeQualifierNickname

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
/**
 * 文件上传数据
 * @property file 文件的base64编码
 * @property name 文件名
 * @property use 文件用途
 * @author dongjak
 * @created 2024/11/20
 * @version 1.0
 * @since 1.0
 */
data class FileUploadData(
    val file: String,
    val name: String,
    val use: String
)

data class LoginResult(
    val token: String,
    val userInfo: UserInfo
)
data class FileUploadResult(
    val url: String
)
data class UpdateProfileRequest(
    val nickname: String? = null,
    val birthday: String? = null,
    val avatarUrl: String? = null,
    val address: String? = null,
    val gender: String? = null,
)

data class UserInfo(
    val id: Long,
    val avatar: String,
    val nickname: String? = null,
    val phoneNumber: String? = null,
    val birthday: String? = null,
    val address: String? = null,
    val gender: String? = null,
)
