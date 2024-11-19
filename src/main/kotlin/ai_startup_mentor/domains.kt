package ai_startup_mentor

import io.ebean.annotation.DbDefault
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import jzeus.db.BaseModel
import java.time.LocalDateTime

@MappedSuperclass
open class AsmBaseModel : BaseModel {
    @Id
    var id: Long? = null

    @WhenCreated
    var creationTime: LocalDateTime? = null

    @WhenModified
    var updateTime: LocalDateTime? = null


}

/**
 * 用户表
 */
@Entity
@Table(name = "asm_users")
@AttributeOverrides(
    AttributeOverride(name = "id", column = Column(name = "id", nullable = false)),
    AttributeOverride(name = "creationTime", column = Column(name = "creation_time")),
    AttributeOverride(name = "updateTime", column = Column(name = "update_time"))
)
open class AsmUser : AsmBaseModel() {
    /**
     * 用户名
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    open var username: String? = null

    /**
     * 密码（加密存储）
     */
    @Size(max = 100)
    @Column(name = "password", length = 100)
    open var password: String? = null

    /**
     * 手机号
     */
    @Size(max = 20)
    @Column(name = "phone", length = 20)
    open var phone: String? = null

    /**
     * 头像URL
     */
    @Lob
    @Column(name = "avatar_url")
    open var avatarUrl: String? = null

    /**
     * 微信OpenID
     */
    @Size(max = 50)
    @Column(name = "openid", length = 50)
    open var openid: String? = null

    /**
     * 用户类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    @DbDefault("'LOCAL'")
    open var userType: Type = Type.LOCAL

    enum class Type {
        WECHAT,
        LOCAL
    }

    class Builder {
        private val instance = AsmUser()
        fun id(id: Long? = null) = apply { instance.id = id }
        fun username(username: String? = null) = apply { instance.username = username }
        fun password(password: String? = null) = apply { instance.password = password }
        fun phone(phone: String? = null) = apply { instance.phone = phone }
        fun avatarUrl(avatarUrl: String? = null) = apply { instance.avatarUrl = avatarUrl }
        fun openid(openid: String? = null) = apply { instance.openid = openid }
        fun userType(userType: Type? = null) = apply { instance.userType = userType ?: Type.LOCAL }
        fun build() = instance
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
