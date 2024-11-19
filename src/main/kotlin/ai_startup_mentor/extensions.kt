package ai_startup_mentor

fun AsmUser.toUserInfo(): UserInfo {
    return UserInfo(
        id = this.id!!,
        avatar = this.avatarUrl ?: "",
        phoneNumber = this.phone
    )
}
