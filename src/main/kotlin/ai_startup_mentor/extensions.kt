package ai_startup_mentor

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop

fun AsmUser.toUserInfo(): UserInfo {
    return UserInfo(
        id = this.id!!,
        avatar = this.avatarUrl ?: "",
        phoneNumber = this.phone
    )
}


val Properties.defaultAvatarUrl: String
    get() = "${this.server.host}/static/avatars/default_avatar.jpg"
