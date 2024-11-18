package ai_startup_mentor

import cn.binarywang.wx.miniapp.api.WxMaService
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.json.JavalinJackson
import jzeus.json.objectMapper
import jzeus.log.LoggerDelegate
import jzeus.net.http.server.*
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
        val request = ctx.bodyAsClass<WechatLoginData>()
        log.info("代码: ${request.code}")
        // Initialize WxMaService
        val wxService = IOC.getComponent(WxMaService::class.java)
        log.info("appid: ${wxService.wxMaConfig.appid}")
        // Get session info
        val sessionInfo: WxMaJscode2SessionResult = wxService.jsCode2SessionInfo(request.code)

        // Decrypt user info using sessionKey if needed
        val userInfo = wxService.userService.getUserInfo(sessionInfo.sessionKey, request.encryptedData, request.iv)
        val wxPhoneInfo = wxService.userService.getPhoneNoInfo(request.code)
        ctx.json(
            ResponsePayloads(
                data = WechatLoginResult(
                    token = "TODO",
                    userInfo = UserInfo(
                        avatar = userInfo.avatarUrl,
                        nickname = userInfo.nickName,
                        phoneNumber = wxPhoneInfo.phoneNumber
                    )
                )
            )
        )
    }

}

fun createHttpServer(port: Int = 8192) {
    configIOC()
    val app = Javalin.create { config ->
        config.jsonMapper(JavalinJackson(objectMapper))
    }.registerRouters("ai_startup_mentor").start(port)
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
