package ai_startup_mentor

import ai_startup_mentor.query.QAsmUser
import cn.binarywang.wx.miniapp.api.WxMaService
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult
import cn.dev33.satoken.stp.StpUtil
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.json.JavalinJackson
import jzeus.db.save
import jzeus.json.objectMapper
import jzeus.json.toJsonString
import jzeus.log.LoggerDelegate
import jzeus.log.log
import jzeus.net.http.server.*
import jzeus.str.bcryptHash
import java.lang.reflect.InvocationTargetException

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
        val request = ctx.bodyAsClass<AccountLoginData>().log(log) {
            "通过账号登录:${this.toJsonString()}"
        }

        val user = QAsmUser().username.eq(request.username).findOne()
            ?: throw UserNotFoundException("找不到用户 ${request.username}")
        if (user.password != request.password.bcryptHash())
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

fun createHttpServer(port: Int = 8192) {
    SaTokenConfigManager.init()
    val app = Javalin.create { config ->
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
