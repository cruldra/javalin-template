package ai_startup_mentor

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop
import java.time.format.DateTimeFormatter

fun AsmUser.toUserInfo(): UserInfo {
    return UserInfo(
        id = this.id!!,
        avatar = this.avatarUrl ?: "",
        phoneNumber = this.phone,
        nickname = this.nickname,
        birthday = this.birthday?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        address = this.address,
        gender = this.gender.name,
    )
}


val Properties.defaultAvatarUrl: String
    get() = "${this.server.host}/static/avatars/default_avatar.jpg"
