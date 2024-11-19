package ai_startup_mentor

import io.javalin.http.staticfiles.Location
import ai_startup_mentor.query.QAsmUser
import cn.binarywang.wx.miniapp.api.WxMaService
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult
import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.stp.StpUtil
import io.javalin.Javalin
import io.javalin.config.MultipartConfig
import io.javalin.config.SizeUnit
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.json.JavalinJackson
import jakarta.servlet.MultipartConfigElement
import jzeus.db.save
import jzeus.json.objectMapper
import jzeus.json.toJsonString
import jzeus.log.LoggerDelegate
import jzeus.log.log
import jzeus.net.http.server.*
import jzeus.str.bcryptHash
import org.springframework.security.crypto.password.PasswordEncoder
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 身份验证相关路由
 * @author dongjak
 * @created 2024/11/19
 * @version 1.0
 * @since 1.0
 */
class AuthorizationRoutes : JavalinRouterDefinitions {
    private val log by LoggerDelegate()

    @PostMapping("/auth/wxLogin")
    fun wxLogin(ctx: Context) {
        val properties = IOC.getComponent(Properties::class.java)
        val request = ctx.bodyAsClass<WechatLoginData>().log(log) {
            "微信登录:${this.toJsonString()}"
        }
        // Initialize WxMaService
        val wxService = IOC.getComponent(WxMaService::class.java)
        log.info("appid: ${wxService.wxMaConfig.appid}")
        // Get session info
        val sessionInfo: WxMaJscode2SessionResult = wxService.jsCode2SessionInfo(request.code)

        // Decrypt user info using sessionKey if needed
        //val userInfo = wxService.userService.getUserInfo(sessionInfo.sessionKey, request.encryptedData, request.iv)
        val wxPhoneInfo =
            wxService.userService.getPhoneNoInfo(sessionInfo.sessionKey, request.encryptedData, request.iv)
        log.info("openid: ${sessionInfo.openid}")
        // 根据手机号查询用户
        val user = QAsmUser().phone.eq(wxPhoneInfo.phoneNumber).findOne() ?: AsmUser().apply {
            phone = wxPhoneInfo.phoneNumber
            openid = sessionInfo.openid
            avatarUrl = properties.defaultAvatarUrl
            nickname = "微信用户"
            username = wxPhoneInfo.phoneNumber // 使用手机号作为用户名
            password = "123456".bcryptHash() // 使用openid的hash作为初始密码
            userType = AsmUser.Type.WECHAT
        }.save()

        // 登录并获取token
        StpUtil.login(user.id)
        val tokenInfo = StpUtil.getTokenInfo()

        ctx.json(
            ResponsePayloads(
                data = LoginResult(
                    token = tokenInfo.tokenValue,
                    userInfo = user.toUserInfo()
                )
            )
        )
    }

    @PostMapping("/auth/login")
    fun login(ctx: Context) {
        val passwordEncoder = IOC.getComponent(PasswordEncoder::class.java)
        val request = ctx.bodyAsClass<AccountLoginData>().log(log) {
            "通过账号登录:${this.toJsonString()}"
        }

        val user = QAsmUser().username.eq(request.username).findOne()
            ?: throw UserNotFoundException("找不到用户 ${request.username}")

        if (!passwordEncoder.matches(request.password, user.password))
            throw InvalidCredentialsException("密码错误")

        // 登录并获取token
        StpUtil.login(user.id)
        val tokenInfo = StpUtil.getTokenInfo()

        ctx.json(
            ResponsePayloads(
                data = LoginResult(
                    token = tokenInfo.tokenValue,
                    userInfo = user.toUserInfo()
                )
            )
        )
    }


}

class UserRoutes : JavalinRouterDefinitions {
    private val log by LoggerDelegate()

    @PostMapping("/user/profile")
    fun updateProfile(ctx: Context) {
        // 检查用户是否登录
        if (!StpUtil.isLogin()) throw AccessDeniedException("请先登录")
        val request = ctx.bodyAsClass<UpdateProfileRequest>().log(log) {
            "更新用户信息:${this.toJsonString()}"
        }

        val loginId = StpUtil.getLoginIdAsLong()
        val user = QAsmUser().id.eq(loginId).findOne()
            ?: throw UserNotFoundException("找不到用户")

        user.apply {
            nickname = request.nickname
            avatarUrl = request.avatarUrl
            birthday =
                request.birthday?.let { LocalDate.parse(request.birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
            address = request.address
            gender = request.gender?.let { AsmUser.Gender.valueOf(request.gender) } ?: AsmUser.Gender.UNKNOWN
        }.save()

        ctx.json(
            ResponsePayloads(
                data = user.toUserInfo()
            )
        )
    }

}

class FileRoutes : JavalinRouterDefinitions {
    private val log by LoggerDelegate()

    @PostMapping("/file/upload")
    fun uploadFile(ctx: Context) {
        // 检查用户是否登录
        if (!StpUtil.isLogin()) throw AccessDeniedException("请先登录")

        val request = ctx.bodyAsClass<FileUploadData>().log(log) {
            "上传文件:${this.name}"
        }

        val fileUse = request.use ?: "common"
        val fileBytes = Base64.getDecoder().decode(request.file)
        val fileName = request.name

        // 确保目录存在
        val directory = File(".data/$fileUse")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // 写入文件
        File(".data/$fileUse/$fileName").writeBytes(fileBytes)


        val properties = IOC.getComponent(Properties::class.java)
        // 返回文件访问路径
        ctx.json(
            ResponsePayloads(
                data = FileUploadResult("${properties.server.host}/static/$fileUse/$fileName")
            )
        )

    }

}

fun createHttpServer(port: Int = 8192) {
    SaTokenConfigManager.init()
    val app = Javalin.create { config ->
        config.http.maxRequestSize = 1024 * 1024 * 1024 // 1GB
        config.staticFiles.add { staticFiles ->
            staticFiles.hostedPath = "/static"
            staticFiles.directory = ".data"
            staticFiles.location = Location.EXTERNAL
        }
        config.jsonMapper(JavalinJackson(objectMapper))
    }
        .registerRouters("ai_startup_mentor")
        //.before(SaTokenAuthHandler())
        .start(port)
    // 在每个请求开始时保存 Context
    app.before { ctx ->
        JavalinRequestHolder.setContext(ctx)
    }

    // 在每个请求结束后清理 Context
    app.after { ctx ->
        JavalinRequestHolder.clear()
    }
    app.exception(
        InvocationTargetException::class.java
    ) { e, ctx ->
        e.printStackTrace()
        ctx.internalServerError(
            ResponsePayloads<Unit>(
                error = ResponsePayloads.Error(
                    type = e.targetException.javaClass.simpleName,
                    message = e.targetException.message ?: "Internal Server Error"
                )
            )
        )
    }

}
