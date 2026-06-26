package ro.puk3p.sentinel.account.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.account.dto.AccountView
import ro.puk3p.sentinel.account.dto.GoogleLoginRequest
import ro.puk3p.sentinel.account.dto.LoginRequest
import ro.puk3p.sentinel.account.dto.LoginResponse
import ro.puk3p.sentinel.account.dto.MfaLoginRequest
import ro.puk3p.sentinel.account.dto.RegisterRequest
import ro.puk3p.sentinel.account.security.SESSION_JTI_ATTR
import ro.puk3p.sentinel.account.service.AccountService
import ro.puk3p.sentinel.common.response.ApiResponse

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val accountService: AccountService,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        http: HttpServletRequest,
    ): ApiResponse<LoginResponse> = ApiResponse(success = true, message = "Authenticated", data = accountService.login(request, http))

    @PostMapping("/mfa")
    fun loginMfa(
        @Valid @RequestBody request: MfaLoginRequest,
        http: HttpServletRequest,
    ): ApiResponse<LoginResponse> =
        ApiResponse(
            success = true,
            message = "Authenticated",
            data = accountService.loginMfa(request.mfaToken, request.code, http),
        )

    @PostMapping("/google")
    fun google(
        @Valid @RequestBody request: GoogleLoginRequest,
        http: HttpServletRequest,
    ): ApiResponse<LoginResponse> =
        ApiResponse(
            success = true,
            message = "Authenticated",
            data = accountService.googleLogin(request.idToken, http),
        )

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): ApiResponse<AccountView> = ApiResponse(success = true, message = "Account created", data = accountService.register(request))

    @PostMapping("/logout")
    fun logout(http: HttpServletRequest): ApiResponse<Unit> {
        val username = SecurityContextHolder.getContext().authentication?.name ?: ""
        accountService.logout(http.getAttribute(SESSION_JTI_ATTR) as String?, username, http)
        return ApiResponse(success = true, message = "Logged out", data = Unit)
    }
}
