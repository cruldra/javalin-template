package ai_startup_mentor

class UserNotFoundException(message: String = "用户不存在", code: Int = 4001) : RuntimeException(message)
class InvalidCredentialsException(message: String = "凭据错误", code: Int = 4002) : RuntimeException(message)
class AccessDeniedException(message: String = "拒绝访问", code: Int = 4003) : RuntimeException(message)
